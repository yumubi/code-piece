package io.goji.exp.strProcess;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BreakIteratorDemo  {

   static void extractWords(String target, BreakIterator wordIterator) {

      wordIterator.setText(target);
      int start = wordIterator.first();
      int end = wordIterator.next();

      while (end != BreakIterator.DONE) {
         String word = target.substring(start,end);
         if (Character.isLetterOrDigit(word.charAt(0))) {
            System.out.println(word);
         }
         start = end;
         end = wordIterator.next();
      }
   }

   static void reverseWords(String target, BreakIterator wordIterator) {

      wordIterator.setText(target);
      int end = wordIterator.last();
      int start = wordIterator.previous();

      while (start != BreakIterator.DONE) {
         String word = target.substring(start,end);
         if (Character.isLetterOrDigit(word.charAt(0)))
            System.out.println(word);
         end = start;
         start = wordIterator.previous();
      }
   }

   static void markBoundaries(String target, BreakIterator iterator) {

      StringBuffer markers = new StringBuffer();
      markers.setLength(target.length() + 1);
      for (int k = 0; k < markers.length(); k++) {
         markers.setCharAt(k,' ');
      }

      iterator.setText(target);
      int boundary = iterator.first();

      while (boundary != BreakIterator.DONE) {
         markers.setCharAt(boundary,'^');
         boundary = iterator.next();
      }

      System.out.println(target);
      System.out.println(markers);
   }

   static void formatLines(String target, int maxLength,
                           Locale currentLocale) {

      BreakIterator boundary = BreakIterator.getLineInstance(currentLocale);
      boundary.setText(target);
      int start = boundary.first();
      int end = boundary.next();
      int lineLength = 0;

      while (end != BreakIterator.DONE) {
         String word = target.substring(start,end);
         lineLength = lineLength + word.length();
         if (lineLength >= maxLength) {
            System.out.println();
            lineLength = word.length();
         }
         System.out.print(word);
         start = end;
         end = boundary.next();
      }
   }

   static void listPositions(String target, BreakIterator iterator) {

      iterator.setText(target);
      int boundary = iterator.first();

      while (boundary != BreakIterator.DONE) {
         System.out.println (boundary);
         boundary = iterator.next();
      }
   }

   static void characterExamples() {

      BreakIterator arCharIterator =
         BreakIterator.getCharacterInstance(new Locale ("ar","SA"));
      // Arabic word for "house"
      String house = "\u0628" + "\u064e" + "\u064a" +
                     "\u0652" + "\u067a" + "\u064f";
      listPositions (house,arCharIterator);
   }

   static void wordExamples() {

      Locale currentLocale = new Locale ("en","US");
      BreakIterator wordIterator =
         BreakIterator.getWordInstance(currentLocale);
      String someText = "She stopped.  " +
                        "She said, \"Hello there,\" and then went on.";
      markBoundaries(someText, wordIterator);
      System.out.println();
      extractWords(someText, wordIterator);
   }

   static void sentenceExamples() {

      Locale currentLocale = new Locale ("en","US");
      BreakIterator sentenceIterator =
         BreakIterator.getSentenceInstance(currentLocale);
      String someText = "She stopped.  " +
                        "She said, \"Hello there,\" and then went on.";
      markBoundaries(someText, sentenceIterator);
      String variousText = "He's vanished!  " +
                           "What will we do?  It's up to us.";
      markBoundaries(variousText, sentenceIterator);
      String decimalText = "Please add 1.5 liters to the tank.";
      markBoundaries(decimalText, sentenceIterator);
      String  donneText = "\"No man is an island . . . " +
                          "every man . . . \"";
      markBoundaries(donneText, sentenceIterator);
      String dogText = "My friend, Mr. Jones, has a new dog.  " +
                       "The dog's name is Spot.";
      markBoundaries(dogText, sentenceIterator);
   }

   static void lineExamples() {

      Locale currentLocale = new Locale ("en","US");
      BreakIterator lineIterator =
        BreakIterator.getLineInstance(currentLocale);
      String someText = "She stopped.  " +
                        "She said, \"Hello there,\" and then went on.";
      markBoundaries(someText, lineIterator);
      String hardHyphen = "There are twenty-four hours in a day.";
      markBoundaries(hardHyphen, lineIterator);
      System.out.println();
      String moreText = "She said, \"Hello there,\" and then " +
                        "went on down the street.  When she stopped " +
                        "to look at the fur coats in a shop window, " +
                        "her dog growled.  \"Sorry Jake,\" she said. " +
                        " \"I didn't know you would take it personally.\"";
      formatLines(moreText, 30, currentLocale);
      System.out.println();
   }

   static public void main(String[] args) {
//
//      characterExamples();
//      System.out.println();
//      wordExamples();
//      System.out.println();
//      sentenceExamples();
//      System.out.println();
//      lineExamples();

      String chineseWords = "舰长微侧过头，斜眼看向几乎要把头埋进自己脖颈处的丽塔。相比于平常总是想着法子来调戏自己尝试让自己面红耳赤，现在的她——恬静 ，却又带有一丝妩媚。湿热的鼻息一次又一次的拍打着自己的脖颈处，微微翘起的嘴角仿佛在告诉着对方，她似乎在做着什么好梦，脸颊也泛起淡淡的红晕" +
              " 我是中国人, 我爱我的祖国, 中国是一个伟大的国家, 中国5000的历史. 上海人喜欢夹杂英语, 比如good morning, afternoon. Fashion哦, 这就是沪✌的生活.";
        BreakIterator bi = BreakIterator.getWordInstance(Locale.CHINESE);
        bi.setText(chineseWords);
        // 将汉语单词都收集到一个列表中
      List<String> chineseWordsList = new ArrayList<>();
        int start = bi.first();
        int end = bi.next();
        while (end != BreakIterator.DONE) {
            String word = chineseWords.substring(start, end);
            if(String.valueOf(word.charAt(0)).matches("[\u4e00-\u9fa5]")) {
                chineseWordsList.add(word);
            }
            start = end;
            end = bi.next();
        }
        chineseWordsList.forEach(System.out::println);

        // 测试句子的分割

      System.out.println("=====================================");
      BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(Locale.CHINESE);
        sentenceIterator.setText(chineseWords);
        int start1 = sentenceIterator.first();
        int end1 = sentenceIterator.next();
        while (end1 != BreakIterator.DONE) {
            String sentence = chineseWords.substring(start1, end1);
            System.out.println(sentence);
            start1 = end1;
            end1 = sentenceIterator.next();
        }




   }

} // class
