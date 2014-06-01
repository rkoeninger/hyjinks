(ns hyjinks)

(use '[clojure.string :only (escape)])

;; General helpers

(defn- html-escape [s]
	(if-not (string? s) s (escape s {
		\< "&lt;"
		\> "&gt;"
		\& "&amp;"
		\" "&quot;"
		\' "&#39;"})))

(defn- str-k [s] (if (keyword? s) (name s) s))

(defn- str-join [& items] (apply str (map str-k (flatten items))))

;; Core types

(defrecord Tag [tag-name attrs css items]
	java.lang.Object
	(toString [this]
		(let [attrs-css (if (empty? css) attrs (assoc attrs :style (str css)))]
			(str-join
				"<" tag-name attrs-css
				(if (empty? items)
					" />"
					[">" (map html-escape items) "</" tag-name ">"])))))

(defmethod print-method Tag [t ^java.io.Writer w] (.write w (str t)))

(defrecord Attrs []
	java.lang.Object
	(toString [this]
		(str-join (map (fn [[k v]] [" " k "=\"" v "\""]) this))))

(defrecord Css []
	java.lang.Object
	(toString [this]
		(str-join (map (fn [[k v]] ["; " k ": " v]) this) ";")))

;; Builder functions

(def attrs0 (Attrs.))

(def css0 (Css.))

(defn attrs? [x] (and (or (instance? Attrs x) (map? x)) (not (instance? Css x)) (not (instance? Tag x))))

(defn css? [x] (instance? Css x))

(defn child-item? [x] (not (or (attrs? x) (css? x))))

(defn attrs+ [tag & key-vals]
	(assoc tag :attrs (merge (:attrs tag) (apply hash-map key-vals))))

(defn css+ [tag & key-vals]
	(assoc tag :css (merge (:css tag) (apply hash-map key-vals))))

(defn attrs* [& key-vals]
	(merge attrs0 (apply hash-map key-vals)))

(defn css* [& key-vals]
	(merge css0 (apply hash-map key-vals)))

(defn tag* [nm & stuff]
	(let [attrs (apply merge attrs0 (filter attrs? stuff))
	      css (apply merge css0 (filter css? stuff))
	      items (flatten (filter child-item? stuff))]
		(Tag. nm attrs css items)))

;; Declaring a whole bunch of tags

(defn declare-tag [sym] (eval `(defn ~sym [& ~'items] (apply tag* ~(str sym) ~'items))))

(dorun (map declare-tag [
	'h1 'h2 'h3 'h4 'h5 'h6
	'b 'i 'u 's 'del 'ins 'small 'sup 'sub 'pre 'q 'cite 'mark 'dbo
	'a 'img 'embed 'object 'param
	'ul 'ol 'li 'dl 'dt 'dd
	'p 'span 'div 'nav 'br 'canvas 'textarea 'blockquote
	'table 'thead 'tbody 'tfoot 'th 'tr 'td 'caption 'colgroup 'col
	'address 'article 'header 'footer 'main 'section 'aside 'figure 'figcaption
	'form 'legend 'fieldset 'select 'input 'button
	'html 'head 'title 'style 'base 'body 'noscript]))

;; Tags with specific features

(defn !-- [& content] (str-join "<!-- " content " -->"))

(defn audio [controls & items] (tag* "audio" {:controls controls} items))

(defn video [controls & items] (tag* "video" {:controls controls} items))

(defn media-source [url type] (tag* "source" {:src url :type type}))

(defn track [url kind lang label] (tag* "track" {:src url :kind kind :srclang lang :label label}))

(defn abbr [title & items] (tag* "abbr" {:title title} items))

(defn datetime
	([content] (tag* "time" content))
	([timestring content] (tag* "time" {:datetime timestring} content)))

(defn option
	([value] (tag* "option" {:value value} value))
	([label value] (tag* "option" {:value value} label)))

(defn optgroup [label & opts] (tag* "optgroup" {:label label} opts))

(defn iframe [url] (tag* "iframe" {:src url}))

(defn progress [value maximum] (tag* "progress" {:value value :max maximum}))

(defn label [target-id text] (tag* "label" {:for target-id} text))

(defn radio [id value param] (tag* "input" {:id id :value value :name param :type "radio"}))

(defn hidden-value [id value] (tag* "input" {:id id :value value :type "hidden"}))

(defn page-meta [prop value] (tag* "meta" {:name prop :content value}))

(defn page-link [rel url] (tag* "link" {:rel rel :href url}))

(defn script
	([syntax] (tag* "script" [syntax]))
	([lang syntax] (tag* "script" {:language lang} syntax)))

(defn import-script
	([url] (tag* "script" {:src url :charset "utf-8"} [""]))
	([lang url] (tag* "script" {:language lang :src url :charset "utf-8"} [""])))

;; Higher-order "tags"

(defn ul-li [& items] (ul (map li (flatten items))))

(defn ol-li [& items] (ol (map li (flatten items))))

(defn tr-td [& items] (tr (map td (flatten items))))

(defn table-tr-td [& rows] (table (map (fn [row] (tr-td (flatten row))) rows)))

(defn dl-dt-dd [term-map]
	(dl (mapcat (fn [[t d]] [(dt t) (dd d)]) (sort-by key term-map))))

(defn radio-list [param & opts]
	(mapcat (fn [[t v]] [(label v t) (radio v v param)]) opts))

;; Common shortcut "tags"

(defn submit-button [] (tag* "button" {:type "submit"} "Submit"))

(defn reset-button [] (tag* "button" {:type "reset"} "Reset"))

(defn stylesheet [url] (tag* "link" {:href url :rel "stylesheet" :media "all" :type "text/css"}))

(defn import-jquery
	([] (import-jquery "1"))
	([version] (import-script "javascript"
		(str "http://ajax.googleapis.com/ajax/libs/jquery/" version "/jquery.min.js"))))

;; Attribute/CSS Decorators
;; Each function has a version that can be inserted in a tag's children:
;;     (div (center) "Contents")
;;     (p (color :red) "Contents")
;; And another version that takes an additional argument and decorates it:
;;     (center some-tag)
;;     (color :red some-tag)

(defn hide
	([] (css* :display "none"))
	([tag] (css+ tag :display "none")))

(defn center
	([] (css* :margin "0 auto" :text-align "center"))
	([tag] (css+ tag :margin "0 auto" :text-align "center")))

(defn color
	([color-code] (css* :color color-code))
	([color-code tag] (css+ tag :color color-code)))