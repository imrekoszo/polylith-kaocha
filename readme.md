# polylith-kaocha

Simple [Kaocha](https://github.com/lambdaisland/kaocha )-based test runner implementation
for [Polylith](https://github.com/polyfy/polylith/).

Created as a demonstration for
Polylith's [WIP pluggable test runner support](https://github.com/polyfy/polylith/pull/196), it is very basic and only
aims to support `clojure.test` for now.

## Usage

Clojure CLI/deps.edn coordinates:

```clojure
io.github.imrekoszo/polylith-kaocha {:git/tag   "v0.2.0"
                                     :git/sha   "0d76766"
                                     :deps/root "projects/test-runner"}
```

Add the above to the `:poly` alias of the root `deps.edn` in your Polylith workspace, along with a reference to a
version of `polylith/clj-poly` which
includes [pluggable test runner support](https://github.com/polyfy/polylith/pull/196):

```clojure
{

 :aliases
 {:poly
  {:extra-deps
   {polylith/clj-poly
    {:git/url   "https://github.com/polyfy/polylith"
     :git/sha   "1c4c7f732149cbd81260b9a4ddc08d913c37d3ff"
     :deps/root "projects/poly"}

    io.github.imrekoszo/polylith-kaocha
    {:git/tag   "v0.2.0"
     :git/sha   "0d76766"
     :deps/root "projects/test-runner"}}}}

 }
```

After which you can configure your projects to be tested with it in `workspace.edn`:

```clojure
{

 ;; To use it as the default test runner in the workspace
 :test {:make-test-runner polylith-kaocha.test-runner.core/make}

 :projects
 {
  ;; To only use it for specific projects
  "foo" {:test {:make-test-runner polylith-kaocha.test-runner.core/make}}

  ;; To revert to the default test runner only for specific projects
  "bar" {:test {:make-test-runner :default}}}

 }
```

## Development

This repository itself is a [Polylith](https://github.com/polyfy/polylith/) workspace, so those conventions apply.
