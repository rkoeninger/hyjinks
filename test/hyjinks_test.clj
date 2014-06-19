(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn should-equal-str
	"Asserts that all values in `xs` have equivalent `str` values"
	[& xs] (is (apply = (map str xs))))

(defmacro should-fail [expr]
	`(try ~expr false (catch java.lang.AssertionError e# true)))

(deftest feature-tour

	; Each tag has a function named for it
	(should-equal-str
		(p "Content")
		"<p>Content</p>")
	(should-equal-str
		(ol (li "Monday") (li "Tuesday") (li "Wednesday"))
		"<ol><li>Monday</li><li>Tuesday</li><li>Wednesday</li></ol>")

	; Tag attributes can be specified with hash-maps
	(should-equal-str
		(p {:attr :value} "Content")
		"<p attr=\"value\">Content</p>")

	; Tags can be composed like normal functions
	(should-equal-str
		(ul (map li ["A" "B" "C"]))
		"<ul><li>A</li><li>B</li><li>C</li></ul>")

	; String content gets escaped, unless it's a literal
	(should-equal-str
		(p "<Content>")
		"<p>&lt;Content&gt;</p>")
	(should-equal-str
		(p (literal "<Content>"))
		"<p><Content></p>")

	; Empty tags have trailing '/' for XHTML compliance
	(should-equal-str
		(p (tag "Content"))
		"<p><Content /></p>")

	; Tags can have special rendering options specified, like :both-tags
	(should-equal-str
		script
		"<script></script>")

	; and :no-escape
	(should-equal-str
		(script "\"")
		"<script>\"</script>")

	; Normally, it's
	(should-equal-str
		(p "\"")
		"<p>&quot;</p>")

	; CSS can be applied with a CSS object
	(should-equal-str
		(div (css :color :red) (br))
		"<div style=\"; color: red;\"><br /></div>")

	; Empty strings and nil get ignored
	(should-equal-str
		(p)
		(p "")
		(p nil)
		(p "" nil))
	(should-equal-str
		(div "A" "" nil "B" "")
		(div nil nil "A" "B" ""))
	
	; Tag can just be a constant if it doesn't need arguments
	(should-equal-str
		(br)
		br
		"<br />")

	; CSS properties can be constants if they don't need arguments
	; And can be used as functions
	(should-equal-str
		(table (hide) (tr (td "A") (td "B")))
		(table hide (tr (td "A") (td "B")))
		(hide (table (tr (td "A") (td "B"))))
		"<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>")

	; Some decorators are variadic and take a Tag as the optional last argument
	(should-equal-str
		(transform (rotate 45) (skew 10 15) div)
		(div (transform (rotate 45) (skew 10 15))))

	; But Tags shouldn't be anywhere else
	(should-fail (transform (rotate 45) div (skew 10 15)))

	; Nullary application should be idempotent - and equal to unapplied tag
	(should-equal-str
		div
		(div)
		((div))
		(((div)))
		((((div)))))

	; Applying tag as function should be same as extend-tag
	(should-equal-str
		(extend-tag (div (center)) (color :red))
		(div (center) (color :red)))
	(should-equal-str
		((div (center)) (color :red))
		(div (center) (color :red)))

	; This allows for easy specialization of tags
	(def special-div (div {:class "special"} (center)))
	(should-equal-str
		(special-div "Hi")
		(div (center) {:class "special"} "Hi"))

	; Test tag application with many arguments - up to 20
	(should-equal-str
		(p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j")
		(apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])
		(p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])
		"<p>abcdefghij</p>")

	; Test tag application with many arguments - over 20
	(should-equal-str
		(p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z")
		(apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])
		(p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])
		"<p>abcdefghijklmnopqrstuvwxyz</p>"))