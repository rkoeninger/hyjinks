(ns hyjinks.macros)

(defmacro deftag [sym & stuff]
  `(def ~sym
    ~(str "The <" (name sym) "> tag.")
    (~'tag ~(name sym) ~@stuff)))

(defmacro deftags [& syms]
  `(do
    ~@(map
      (fn [sym] `(~'deftag ~sym))
      syms)))

(defn- invoke-params [n]
  (map #(symbol (str "x" %)) (range n)))

(defn- cljs-invoke [n]
  (let [params (invoke-params n)]
  `([~'this ~@params] (~'extend-tag ~'this ~@params))))

(defmacro cljs-tag-ifn [type]
  `(~'extend-type ~type
    cljs.core/IFn
    (~'-invoke
      ([~'this] ~'this)
      ~@(map cljs-invoke (map inc (range 20)))
      ~(let [params (invoke-params 20)]
        `([~'this ~@params ~'more] (~'apply ~'extend-tag ~'this ~@params ~'more))))))

(defn- clj-invoke [n]
  (let [params (invoke-params n)]
  `(~'invoke [~'this ~@params] (~'extend-tag ~'this ~@params))))

(defmacro clj-tag-ifn [decl]
  (concat
    decl
    ['clojure.lang.IFn
     '(invoke [this] this)]
    (map clj-invoke (map inc (range 20)))
    (let [params (invoke-params 20)]
      [`(~'invoke [~'this ~@params ~'more] (~'apply ~'extend-tag ~'this ~@params ~'more))])
    [`(~'applyTo [~'this ~'args] (~'apply ~'extend-tag ~'this ~'args))]))
