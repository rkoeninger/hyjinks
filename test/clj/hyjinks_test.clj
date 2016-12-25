(ns hyjinks-test
  (:use hyjinks.core)
  (:require [clojure.test :refer [deftest is testing]]))

(deftest basic-tag-operations
  (testing "tags get applied just like functions"
    (is (= "<p>content</p>"
           (str (p "content")))))

  (testing "applying a tag as a function is just like using extend-tag"
    (is (= (div center (color :red))
           (extend-tag div center (color :red)))))

  (testing "new functions can be created by application of tags"
    (let [big-blue-header (h1 center (color :blue))]
      (is (= (h1 (color :blue) center "Hi")
             (big-blue-header "Hi")))))

  (testing "tags can be composed like normal functions"
    (is (= "<div>Lorem ipsum<em>dolor</em>sit amet</div>"
           (str (div "Lorem ipsum" (em "dolor") "sit amet")))))

  (testing "tag function can be mapped over arguments"
    (is (= "<ol><li>Monday</li><li>Tuesday</li><li>Wednesday</li></ol>"
           (str (ol (map li ["Monday" "Tuesday" "Wednesday"]))))))

  (testing "argument list gets automatically flattened into one long list"
    (is (= (p 1 2 3)
           (p [1 2 3])
           (p [1] [2 3])
           (p 1 [[2]] 3))))

  (testing "empty strings and nil don't effect output"
    (is (= (p)
           (p "")
           (p nil)
           (p "" nil)))
    (is (= (div "A" "" nil "B" "")
           (div nil nil "A" "B" ""))))

  (testing "non-string child items get toString'd"
    (is (= "<p>1</p>" (str (p 1))))
    (is (= "<p>false</p>" (str (p false))))
    (is (= "<p>falseqwe43asd</p>" (str (p false "qwe" 4 3 "asd"))))
    (is (= "<p>falseqwe<p>zxc</p>43asd</p>" (str (p false "qwe" (p "zxc") 4 3 "asd"))))))

(deftest attributes
  (testing "tag attributes can be specified with plain hash-maps"
    (is (= "<p attr=\"value\">Content</p>"
           (str (p {:attr "value"} "Content"))))))

(deftest styling
  (testing "inline css can be specified using a property list"
    (is (= "<div style=\"; color: red;\"><br></div>"
           (str (div (css :color "red") (br)))))))

(deftest decorators
  (testing "decorator can be a function on tags or argument to tag as function"
    (is (= (center div) (div center)))
    (is (= (color "red" div) (div (color "red")))))

  (testing "decorators can be used without being applied if they don't need arguments"
    (is (= "<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>"
           (str (table (hide) (tr (td "A") (td "B"))))
           (str (table hide (tr (td "A") (td "B"))))
           (str (hide (table (tr (td "A") (td "B")))))))))

(deftest rendering-options
  (testing "extra padding between non-tag elements can be specified"
    (is (= "<p>false qwe<p>zxc</p>4 3 asd</p>"
           (str (p pad-children false "qwe" (p "zxc") 4 3 "asd"))))))

(deftest html-escaping
  (testing "string content gets escaped"
    (is (= "<p>&lt;content&gt;</p>"
           (str (p "<content>"))))
    (is (= "<p>&quot;</p>"
           (str (p "\"")))))

  (testing "literals do not escape their contents"
    (is (= "<p><content></p>"
           (str (p (literal "<content>"))))))

  (testing "script tag has no-escape enabled by default"
    (is (= "<script>\"</script>"
           (str (script "\""))))))

(deftest void-elements
  (testing "when void elements are serialized, they don't have a closing tag"
    (is (= "<br>"
           (str br)))))

(deftest tag-selector-syntax
  (testing "default tag name is \"div\""
    (let [{:keys [tag-name attrs]} (tag "#me.class1.class2")]
      (is (= "div" tag-name))
      (is (= "me" (:id attrs)))
      (is (= "class1 class2" (:className attrs)))))

  (testing "empty string specifies no id, classes or tag-name (defaults to \"div\")"
    (let [{:keys [tag-name]} (tag "")]
      (is (= "div" tag-name))))

  (testing "many classes can be specified"
    (let [{:keys [attrs]} (tag ".class1.class2.class3.class4")]
      (is (= "class1 class2 class3 class4" (:className attrs)))))

  (testing "selector syntax can be given as a string, keyword or symbol"
    (is (= (tag "div.clear#content")
           (tag :div.clear#content)
           (tag 'div.clear#content)))))

(deftest interchangeability-of-strings-keywords-symbols
  (testing "attr names should be same whether in the form of strings, keywords or symbols"
    (is (= (str (attrs "title" "hi"))
           (str (attrs :title "hi"))
           (str (attrs 'title "hi")))))

  (testing "css property names should be same whether in the form of strings, keywords or symbols"
    (is (= (str (css "color" "red"))
           (str (css :color "red"))
           (str (css 'color "red"))))))

(deftest function-arities
  (testing "tag function itself is same as tag function applied to zero args"
    (is (= br
           (br)))
    (is (= (str br)
           (str (br))
           "<br>")))

  (testing "tag function should be able to handle up to 20 args"
    (is (= (p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j")
           (apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])
           (p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j"])))
    (is (= (str (p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j"))
           "<p>abcdefghij</p>")))

  (testing "tag function should be able to handle more than 20 args"
    (is (= (p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z")
           (apply p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])
           (p ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"])))
    (is (= (str (p "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"))
           "<p>abcdefghijklmnopqrstuvwxyz</p>"))))

(deftest nullary-application
  (testing "when tag/attrs/css functions are applied to no args, should be same"
    (is (= div (div) ((div))))
    (is (= (color :red) ((color :red))))
    (is (= (attrs :title "hi") ((attrs :title "hi"))))))

(deftest child-items
  (testing "when some child items are lists"
    (let [t (div "numbers:" [1 2 3])]

      (testing "child items should be flattened into one long list"
        (is (= (:items t) ["numbers:" 1 2 3]))))))

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
