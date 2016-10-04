(ns hyjinks-test
  (:use hyjinks.core)
  (:use clojure.test))

(defn should-equal-str
  "Asserts that all values in `xs` have equivalent `str` values"
  [& xs] (is (apply = (map str xs))))

(defmacro should-fail [expr]
  `(try ~expr false (catch java.lang.Throwable e# true)))

(deftest feature-tour

  ; Each tag has a function named for it
  (should-equal-str
    (p "Content")
    "<p>Content</p>")
  (should-equal-str
    (ol (li "Monday") (li "Tuesday") (li "Wednesday"))
    "<ol><li>Monday</li><li>Tuesday</li><li>Wednesday</li></ol>")

  ; Non-strings can be passed as tag contents
  (should-equal-str
    (p 1)
    "<p>1</p>")
  (should-equal-str
    (p false)
    "<p>false</p>")
  (should-equal-str
    (p false "qwe" 4 3 "asd")
    "<p>falseqwe43asd</p>")
  (should-equal-str
    (p false "qwe" (p "zxc") 4 3 "asd")
    "<p>falseqwe<p>zxc</p>43asd</p>")

  ; Extra padding between non-tag elements can be specified
  (should-equal-str
    (p pad-children false "qwe" (p "zxc") 4 3 "asd")
    "<p>false qwe<p>zxc</p>4 3 asd</p>")

  ; Tag attributes can be specified with hash-maps
  (should-equal-str
    (p {:attr "value"} "Content")
    "<p attr=\"value\">Content</p>")

  ; Tags can be composed like normal functions
  (should-equal-str
    (ul (map li ["A" "B" "C"]))
    "<ul><li>A</li><li>B</li><li>C</li></ul>")

  ; String content gets escaped, unless it's a literal
  (should-equal-str
    (p "<content>")
    "<p>&lt;content&gt;</p>")
  (should-equal-str
    (p (literal "<content>"))
    "<p><content></p>")

  ; Empty tags have trailing '/' for XHTML compliance
  (should-equal-str
    (p (tag "content"))
    "<p><content></content></p>")

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
    (div (css :color "red") (br))
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
    (center div)
    (div center))

  ; But Tags shouldn't be anywhere else
  (should-fail (color div "red"))

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
    "<p>abcdefghijklmnopqrstuvwxyz</p>")

  ; tag function can take string/symbol/keyword with css selector syntax
  (is (= (:attrs (tag "div.clear#content")) (attrs :id "content" :className "clear")))
  (is (= (:attrs (tag 'div.clear#content)) (attrs :id "content" :className "clear")))
  (is (= (:attrs (tag :div.clear#content)) (attrs :id "content" :className "clear"))))

(deftest class-name-attribute
  (testing "when :className attribute is set to a list"
    (let [t (div {:className ["a" "b" "c"]})]
      (is (= "a b c" (get-in t [:attrs :className])))

      (testing "it should be converted to a space-separated list"
        (is (= (tag->string t) "<div class=\"a b c\"></div>")))

      (testing "and tag is applied to additional :className attribute"
        (let [t (t {:className ["d" "e"]})]

          (testing ":className should be flattened into a string"
            (is (= "a b c d e" (get-in t [:attrs :className])))
            (is (= (tag->string t) "<div class=\"a b c d e\"></div>")))))))

  (testing "when :className attribute is set to an empty list"
    (let [t (div {:className []})]

      (testing "it should not be emitted"
        (is (= (tag->string t) "<div></div>")))))

  (testing "nil values should be ignored in className list"
    (let [t (div {:className ["abc" nil "def"]})]
      (is (= (tag->string t) "<div class=\"abc def\"></div>"))))

  (testing "should be able to combine class names given as list with class name given as string"
    (let [t ((tag "div.abc" {:className "def"}) {:className "ghi"})]
      (is (= (get-in t [:attrs :className]) "abc def ghi")))))
