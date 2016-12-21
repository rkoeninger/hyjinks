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
