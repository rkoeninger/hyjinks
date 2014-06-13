(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn is=str
	"Asserts that all values in `xs` have equivalent `str` values"
	[& xs] (is (apply = (map str xs))))

(defmacro should-fail [expr]
	`(try ~expr false (catch java.lang.AssertionError e# true)))

(deftest feature-tour

	; Each tag has a function named for it
	(is=str
		(p "Content")
		"<p>Content</p>")
	(is=str
		(ol (li "Monday") (li "Tuesday") (li "Wednesday"))
		"<ol><li>Monday</li><li>Tuesday</li><li>Wednesday</li></ol>")

	; Tag attributes can be specified with hash-maps
	(is=str
		(p {:attr :value} "Content")
		"<p attr=\"value\">Content</p>")

	; Tags can be composed like normal functions
	(is=str
		(ul (map li ["A" "B" "C"]))
		"<ul><li>A</li><li>B</li><li>C</li></ul>")

	; String content gets escaped, unless it's a literal
	(is=str
		(p "<Content>")
		"<p>&lt;Content&gt;</p>")
	(is=str
		(p (literal "<Content>"))
		"<p><Content></p>")

	; Empty tags have trailing '/' for XHTML compliance
	(is=str
		(p (tag "Content"))
		"<p><Content /></p>")

	; CSS can be applied with a CSS object
	(is=str
		(div (css :color :red) (br))
		"<div style=\"; color: red;\"><br /></div>")

	; Empty strings and nil get ignored
	(is=str
		(p)
		(p "")
		(p nil)
		(p "" nil))
	(is=str
		(div "A" "" nil "B" "")
		(div nil nil "A" "B" ""))
	
	; Tag can just be a constant if it doesn't need arguments
	(is=str
		(br)
		br
		"<br />")

	; CSS properties can be constants if they don't need arguments
	; And can be used as functions
	(is=str
		(table (hide) (tr (td "A") (td "B")))
		(table hide (tr (td "A") (td "B")))
		(hide (table (tr (td "A") (td "B"))))
		"<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>")
	(is=str
		(map-table {:first-name "Rusty" :last-name "Shackelford"})
		"<table><tr><td>First Name</td><td>Rusty</td></tr><tr><td>Last Name</td><td>Shackelford</td></tr></table>")

	; Some decorators are variadic and take a Tag as the optional last argument
	(is=str
		(transform (rotate 45) (skew 10 15) div)
		(div (transform (rotate 45) (skew 10 15))))

	; But Tags shouldn't be anywhere else
	(should-fail (transform (rotate 45) div (skew 10 15)))

	; This test is essentially broken as attributes are sorted
	; by hash function and not in any user-reasonable manner.
	(is=str
		(tag "asd" {:a 4 :b :d} "qwe" {:q :e :f 4} (css :asd "qwe" :dfg :ert) "sdf")
		"<asd style=\"; asd: qwe; dfg: ert;\" q=\"e\" f=\"4\" b=\"d\" a=\"4\">qwesdf</asd>")

	; Unary application should be idempotent - and equal to unapplied tag
	(is=str
		div
		(div)
		((div))
		(((div)))
		((((div)))))

	; Applying tag as function should be same as extend-tag
	(is=str
		(extend-tag (div (center)) (color :red))
		(div (center) (color :red)))
	(is=str
		((div (center)) (color :red))
		(div (center) (color :red)))

	; This allows for easy specialization of tags
	(def special-div (div {:class "special"} (center)))
	(is=str
		(special-div "Hi")
		(div (center) {:class "special"} "Hi"))

	; Test tag application with many arguments - up to 20
	(is=str
		(p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j")
		(apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])
		(p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])
		"<p>abcdefghij</p>")

	; Test tag application with many arguments - over 20
	(is=str
		(p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z")
		(apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])
		(p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])
		"<p>abcdefghijklmnopqrstuvwxyz</p>"))