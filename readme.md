# polylith-kaocha

Simple [Kaocha](https://github.com/lambdaisland/kaocha )-based test runner
implementation for [Polylith](https://github.com/polyfy/polylith/).

Created as a demonstration for
Polylith's [WIP pluggable test runner support](https://github.com/polyfy/polylith/pull/196)
, it can now use custom Kaocha configuration from resources.

### Disclaimer

This repository aims to serve as an example implementation of a test runner that
can be plugged into a Polylith workspace. The source code itself is organized
into a Polylith workspace for the following reasons:

1. So that it eats its own dogfood, that is, tests for this repository are
   executed using the product of it
2. So that the author can practice setting up a Polylith repository

This repository is not intended to serve as a demonstration of a Polylith
workspace and in places might not be fully idiomatic Polylith.

Breaking changes should be expected, especially until
the [pluggable test runner support](https://github.com/polyfy/polylith/pull/196)
PR gets merged.

Once the PR mentioned is merged, updates to this codebase will probably cease.

## Usage

### 1. Add dep to :poly alias

Add the above to the `:poly` alias of the root `deps.edn` in your Polylith
workspace, along with a reference to a version of `polylith/clj-poly` which
includes [pluggable test runner support](https://github.com/polyfy/polylith/pull/196):

```clojure
{

 :aliases
 {:poly
  {:extra-deps
   {polylith/clj-poly
    {:git/url "https://github.com/polyfy/polylith"
     :git/sha "cce8c7a57ef0c4b58b8fad1c6a9294ac78247c49"
     :deps/root "projects/poly"}

    polylith-kaocha/test-runner
    {:git/url "https://github.com/imrekoszo/polylith-kaocha"
     :git/tag "v0.5.1"
     :git/sha "17a93fa"
     :deps/root "projects/test-runner"}}}}

 }
```

### 2. Reference in workspace config

After which you can configure your projects to be tested with it
in `workspace.edn`:

```clojure
{

 ;; To use it as the default test runner in the workspace
 :test {:create-test-runner polylith-kaocha.test-runner/create}

 :projects
 {
  ;; To only use it for specific projects
  "foo" {:test {:create-test-runner polylith-kaocha.test-runner/create

                ;; Use a specific kaocha configuration from resource
                :polylith-kaocha/config-resource "resource/path/my-tests.edn"}}

  ;; To revert to the default test runner only for specific projects
  "bar" {:test {:create-test-runner :default}}

  ;; To use it in addition to the default test runner
  "baz" {:test {:create-test-runner [:default polylith-kaocha.test-runner/create]}}
  }
 }
```

### 3. Add kaocha wrapper dep to affected projects

Since this test runner will try to invoke Kaocha's own commands via the wrapper
in this workspace in the contexts of projects configured, the wrapper itself
must be added as a test dependency for every such project:

```clojure
{

 :aliases
 {:test
  {:extra-deps
   {polylith-kaocha/kaocha-wrapper
    {:git/url "https://github.com/imrekoszo/polylith-kaocha"
     :git/tag "v0.5.1"
     :git/sha "17a93fa"
     :deps/root "projects/kaocha-wrapper"}}}}

 }
```

### 4. Run tests

```shell
clojure -Srepro -M:poly test :all
```

## Development

This repository itself is a [Polylith](https://github.com/polyfy/polylith/)
workspace, so those conventions, commands etc. mostly apply.

### bb tasks

There are
some [Babashka](https://github.com/babashka/babashka) [tasks](https://book.babashka.org/#tasks)
defined in `bb.edn` to save some typing. Use `bb tasks` to find out more.
