package FIS;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class JTextAreaPrintStream extends PrintStream {

    public JTextAreaPrintStream(JTextArea textArea) {
        super(new JTextAreaOutputStream(textArea));
    }
    static JFrame frame;

    private static class JTextAreaOutputStream extends OutputStream {

        private final JTextArea textArea;

        private JTextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
            textArea.setWrapStyleWord(false);
            textArea.setLineWrap(false);
            textArea.setEditable(false);
            //Font font = new Font("Verdana", Font.PLAIN, 9);
            Font font = new Font("Monospaced", Font.PLAIN, 9);
            textArea.setFont(font);
            //textArea.setCaretPosition(textArea.getDocument().getLength());
            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }

        @Override
        public void write(int i) {
            textArea.append(new String(new char[]{(char) i}));
        }

        @Override
        public void write(byte[] b) {
            textArea.append(new String(b));
        }

        @Override
        public void write(byte[] b, int offset, int len) {
            textArea.append(new String(b, offset, len));
        }
    }

    public static void initGUI() {

        JTextArea text = new JTextArea(20, 100);

        frame = new JFrame("Execution History");
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(text), BorderLayout.CENTER);
        frame.pack();
        //frame.setVisible(true);
        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        System.setOut(new JTextAreaPrintStream(text));
    }
    public static void show(){
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
