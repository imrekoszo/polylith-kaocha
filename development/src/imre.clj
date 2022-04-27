(ns imre)

(comment

  (-> [
       ;"info"
       "test"
       ;":all"
       ;":dev"RE
       ;":project"
       "project:kaocha-wrapper"
       ]
    ((requiring-resolve 'polylith.clj.core.user-input.interface/extract-params))
    ((requiring-resolve 'polylith.clj.core.command.interface/execute-command)))

  )
