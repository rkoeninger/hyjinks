(ns hyjinks.macros)

(defn- docs [tag-name stuff]
  (if (some #{'void-element} stuff)
    (str "The <" tag-name "/> tag.")
    (str "The <" tag-name "></" tag-name "> tag.")))

(defmacro deftag [sym & stuff]
  `(def ~sym
    ~(docs (name sym) stuff)
    (~'tag ~(name sym) ~@stuff)))

(defmacro deftags [& syms]
  (cons 'do (map #(list 'deftag %) syms)))

(defn- invoke-params [n]
  (map #(symbol (str "x" %)) (range n)))

(defn- cljs-invoke [n]
  (let [params (invoke-params n)]
  `([~'this ~@params] (~'extend-tag ~'this ~@params))))

(defmacro extend-type-ifn [type]
  (list
    'extend-type
    type
    'cljs.core/IFn
    `(~'-invoke
      ([~'this] ~'this)
      ~@(map cljs-invoke (map inc (range 20)))
      ~(let [params (invoke-params 20)]
        `([~'this ~@params ~'more] (~'apply ~'extend-tag ~'this ~@params ~'more))))))

(defn- clj-invoke [n]
  (let [params (invoke-params n)]
  `(~'invoke [~'this ~@params] (~'extend-tag ~'this ~@params))))

(defmacro defrecord-ifn [& body]
  (concat
    ['defrecord]
    body
    #?@(
      :clj [
        ['clojure.lang.IFn
         '(invoke [this] this)]
        (map clj-invoke (map inc (range 20)))
        (let [params (invoke-params 20)]
          [`(~'invoke [~'this ~@params ~'more] (~'apply ~'extend-tag ~'this ~@params ~'more))])
        [`(~'applyTo [~'this ~'args] (~'apply ~'extend-tag ~'this ~'args))]]
      :cljs [])))
