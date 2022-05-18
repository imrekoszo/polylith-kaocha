(ns polylith-kaocha.kaocha-test-runner.bricks-to-test
  "Copied from polylith.clj.core.change.bricks-to-test so it doesn't filter out
  bricks that don't have dedicated test sources"
  (:require [clojure.set :as set]))

(defn bricks-to-test-for-project [{:keys [is-dev alias name base-names component-names]}
                                  settings
                                  changed-projects
                                  changed-components
                                  changed-bases
                                  project-to-indirect-changes
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


        ;;
        ;; HERE IS THE CHANGE PART 1
        ;;
        ;; all-brick-names (into #{} (mapcat :test) [base-names component-names])
        ;;
        all-brick-names (into #{} (comp (mapcat vals) cat) [base-names component-names])
        ;;
        ;;
        ;; END OF CHANGE PART 1
        ;;



        ;; If the :test key is given for a project in workspace.edn, then only include
        ;; the specified bricks, otherwise, run tests for all bricks that have tests.
        included-bricks (if-let [bricks (get-in settings [:projects name :test :include])]
                          (set/intersection all-brick-names (set bricks))
                          all-brick-names)
        selected-bricks (if selected-bricks
                          (set selected-bricks)
                          all-brick-names)
        changed-bricks (if include-project?
                         (if (or is-run-all-brick-tests project-has-changed?)
                           ;; if we pass in :all or :all-bricks or if the project has changed
                           ;; then always run all brick tests.
                           included-bricks
                           (set/intersection included-bricks
                             selected-bricks
                             (into #{} cat
                               [changed-components
                                changed-bases
                                (-> name project-to-indirect-changes :src)
                                (-> name project-to-indirect-changes :test)])))
                         #{})
        ;; And finally, if brick:BRICK is given, also filter on that, which means that if we
        ;; pass in both brick:BRICK and :all, we will run the tests for all these bricks,
        ;; whether they have changed or not (directly or indirectly).
        bricks-to-test (set/intersection changed-bricks selected-bricks)]


    ;;
    ;; HERE IS THE CHANGE PART 2
    ;;
    ;; WE ONLY NEED A SET THAT REPRESENTS THE PROJECT'S BRICKS TO TEST
    ;;
    bricks-to-test
    ;;
    ;; END OF CHANGE PART 2
    ;;
    ))

(defn bricks-to-test
  [project {:keys [changes settings user-input] :as _workspace}]
  (let [{:keys [is-dev is-run-all-brick-tests selected-bricks selected-projects]} user-input
        {:keys [changed-components
                changed-bases
                changed-projects
                project-to-indirect-changes]} changes]
    (bricks-to-test-for-project
      project settings changed-projects changed-components changed-bases
      project-to-indirect-changes selected-bricks selected-projects is-dev
      is-run-all-brick-tests)))
