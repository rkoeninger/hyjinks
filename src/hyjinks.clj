(ns hyjinks)

;; General helpers

(defn- str-join [& items] (apply str (flatten items)))

(defn- str-k [x] (if (keyword? x) (name x) x))

(defn- inc-nil [x] (if x (inc x)))

;; Core types

(defprotocol TagProtocol
	(toString [this depth]))

(defrecord Tag [tag-name attrs css items]
	java.lang.Object
		(toString [this] (toString this 0)) ; 0 to enable pretty-printing by default, nil for no pp by default.
	TagProtocol
		(toString [this depth]
			(let [spaces (if depth (str-join (repeat (* 2 depth) " ")))
			      tabbed-in (> (or depth 0) 0)
			      has-children (some (partial instance? Tag) items)
			      attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))
			      str-item (fn [item] (if (instance? Tag item) (toString item (inc-nil depth)) (str-k item)))]
				(str
					spaces
					"<" tag-name attrs-with-css
					(if (empty? items)
						" />"
						(str-join
							">"
							(if (and depth has-children) "\r\n")
							(map str-item items)
							(if has-children spaces)
							"</" tag-name ">"))
					(if tabbed-in "\r\n")))))

(defrecord Attrs []
	java.lang.Object
		(toString [this]
			(str-join (map (fn [[k v]] (str " " (str-k k) "=\"" (str-k v) "\"")) this))))

(defrecord Css []
	java.lang.Object
		(toString [this]
			(str-join (map (fn [[k v]] (str "; " (str-k k) ": " (str-k v))) this) ";")))

;; Builder functions

(def attrs0 (Attrs.))

(def css0 (Css.))

(defn attrs? [x] (and (or (instance? Attrs x) (map? x)) (not (instance? Css x)) (not (instance? Tag x))))

(defn css? [x] (instance? Css x))

(defn attrs+ [tag & key-vals]
	(assoc tag :attrs (merge (:attrs tag) (apply hash-map key-vals))))

(defn child-item? [x] (not (or (attrs? x) (css? x))))

(defn css+ [tag & key-vals]
	(assoc tag :css (merge (:css tag) (apply hash-map key-vals))))

(defn attrs* [& key-vals]
	(merge attrs0 (apply hash-map key-vals)))

(defn css* [& key-vals]
	(merge css0 (apply hash-map key-vals)))

(defn tag* [nm & stuff]
	(let [attrs (apply merge attrs0 (filter attrs? stuff))
	      css (apply merge css0 (filter css? stuff))
	      items (filter child-item? stuff)]
		(Tag. nm attrs css items)))

;; Declaring a whole bunch of functions

(dorun (map (fn [sym] (eval `(def ~sym (partial tag* ~(str sym))))) [
	'h1 'h2 'h3 'h4 'h5 'h6
	'b 'i 'u 's 'del 'ins 'small 'sup 'sub 'pre 'q 'cite 'mark
	'ul 'ol 'li 'dl 'dt 'dd
	'p 'span 'div 'nav 'br 'canvas
	; map/area/img.usemap ?
	'table 'thead 'tbody 'tfoot 'th 'tr 'td 'caption 'colgroup 'col
	'address 'article 'header 'footer 'main 'section 'aside 'figure 'figcaption
	'form 'legend 'fieldset 'select 'datalist
	'html 'head 'title 'style 'body 'noscript]))

;; Tags with specific features

; object/param?

(defn a [url & items] (tag* "a" {:href url} items))

(defn datetime
	([content] (tag* "time" content))
	([timestring content] (tag* "time" {:datetime timestring} content)))

(defn img [url & items] (tag* "img" {:src url} items))

(defn embed [url & items] (tag* "embed" {:src url} items))

(defn audio [controls & items] (tag* "audio" {:controls controls} items))

(defn video [controls & items] (tag* "video" {:controls controls} items))

(defn media-source [url type] (tag* "source" {:src url :type type}))

(defn track [url kind lang label] (tag* "track" {:src url :kind kind :srclang lang :label label}))

(defn abbr [title & items] (tag* "abbr" {:title title} items))

(defn dbo [dir & items] (tag* "dbo" {:dir dir} items))

(defn blockquote [src & items] (tag* "blockquote" {:cite src} items))

(defn option
	([value] (tag* "option" {:value value} value))
	([label value] (tag* "option" {:value value} label)))

(defn optgroup [label & opts] (tag* "optgroup" {:label label} opts))

(defn iframe [url] (tag* "iframe" {:src url}))

(defn progress [value maximum] (tag* "progress" {:value value :max maximum}))

(defn textarea [id rows cols] (tag* "textarea" {:rows rows :cols cols}))

(defn input [id type] (tag* "input" {:id id :type type}))

(defn button [id text] (tag* "button" {:id id} text))

(defn label [target-id text] (tag* "label" {:for target-id} text))

(defn radio [id value param] (tag* "input" {:id id :value value :name param :type "radio"}))

(defn hidden-value [id value] (tag* "input" {:id id :value value :type "hidden"}))

(defn base
	([url] (tag* "base" {:href url}))
	([url target] (tag* "base" {:href url :target target})))

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

(defn dl-dt-dd [term-map]
	(dl (mapcat (fn [[t d]] [(dt t) (dd d)]) (sort-by key term-map))))

(defn table-tr-td [& rows] (table (map (fn [row] (tr-td (flatten row))) rows)))

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

;; Decorators
;; Each function has a version that can be inserted in a tag's children:
;;     (div (center) "Contents")
;;     (p (color :red) "Contents")
;; And another version that takes an additional argument and decorates it:
;;     (center some-tag)
;;     (color :red some-tag)

(defn id
	([i] (attrs* :id i))
	([i tag] (attrs+ tag :id i)))

(defn css-class
	([class-name] (attrs* :class class-name))
	([class-name tag] (attrs+ tag :class class-name)))

(defn width
	([w] (attrs* :width w))
	([w tag] (attrs+ tag :width w)))

(defn height
	([h] (attrs* :height h))
	([h tag] (attrs+ tag :height h)))

(defn hide
	([] (css* :display "none"))
	([tag] (css+ tag :display "none")))

(defn center
	([] (css* :margin "0 auto" :text-align "center"))
	([tag] (css+ tag :margin "0 auto" :text-align "center")))

(defn color
	([color-code] (css* :color color-code))
	([color-code tag] (css+ tag :color color-code)))