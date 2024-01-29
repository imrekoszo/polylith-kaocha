(ns polylith-kaocha.kaocha-test-runner.bricks-to-test
  "Copied from polylith.clj.core.change.bricks-to-test, so it doesn't filter out bricks that don't have dedicated test sources."
  (:require
    [clojure.set :as set]
    [polylith.clj.core.common.interface :as common]))

(defn bricks-to-test-
  "Copied from polylith.

  At commit https://github.com/polyfy/polylith/blob/00579eefcaaa853ede3275e1c6a3395c1f37710b/components/change/src/polylith/clj/core/change/bricks_to_test.clj#L5

  Change indicated inline."
  [{:keys [is-dev alias name test base-names component-names indirect-changes]}
   source
   changed-projects
   changed-bricks
   selected-bricks
   selected-projects
   is-dev-user-input
   is-run-all-brick-tests]
  (let [include-project? (or (or (contains? selected-projects name)
                               (contains? selected-projects alias))
                           (and (empty? selected-projects)
                             (or (not is-dev)
                               is-dev-user-input)))
        project-has-changed? (contains? (set changed-projects) name)
        ;; change
        ;; we need to consider all referenced bricks as Kaocha might discover tests in src-only bricks too
        bricks-for-source (into #{} (comp (mapcat vals) cat) [base-names component-names])
        ;; end change
        included-bricks (common/brick-names-to-test test bricks-for-source)
        selected-bricks (if selected-bricks
                          (set selected-bricks)
                          bricks-for-source)
        changed-bricks (if include-project?
                         (if (or is-run-all-brick-tests project-has-changed?)
                           included-bricks
                           (set/intersection included-bricks
                             selected-bricks
                             (into #{} cat
                               [changed-bricks (source indirect-changes)])))
                         #{})]
    (set/intersection changed-bricks selected-bricks)))

(defn ensure-at-least-0-2-19
  "The function bricks-to-test- operates on >=0.2.19-snapshot data so older projects need to be patched."
  [project settings]
  (cond-> project
    (not (contains? project :test))
    (assoc :test (get-in settings [:projects (:name project) :test]))))

(defn bricks-to-test
  "Copied from polylith.

  At commit https://github.com/polyfy/polylith/blob/00579eefcaaa853ede3275e1c6a3395c1f37710b/components/change/src/polylith/clj/core/change/bricks_to_test.clj#L49

  Changes:
  - not associng into project just returning
  - less lazy"
  [project {:keys [changes settings user-input] :as _workspace}]
  (let [project (ensure-at-least-0-2-19 project settings)
        {:keys [is-dev is-run-all-brick-tests selected-bricks selected-projects]} user-input
        {:keys [changed-components changed-bases changed-projects]} changes]
    (-> #{}
      (into (bricks-to-test- project :src changed-projects changed-bases selected-bricks selected-projects is-dev is-run-all-brick-tests))
      (into (bricks-to-test- project :src changed-projects changed-components selected-bricks selected-projects is-dev is-run-all-brick-tests))
      (into (bricks-to-test- project :test changed-projects changed-bases selected-bricks selected-projects is-dev is-run-all-brick-tests))
      (into (bricks-to-test- project :test changed-projects changed-components selected-bricks selected-projects is-dev is-run-all-brick-tests)))))
