package de.sunnix.srpge.editor.window.script;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class ScriptEditor extends RTextScrollPane {

    private DefaultCompletionProvider provider;

    private List<Completion> completions = new LinkedList<>();

    public ScriptEditor(){
        super(createTextArea());
        setBackground(UIManager.getColor("TextArea.background"));
        setForeground(UIManager.getColor("TextArea.foreground"));
        createProvider();
    }

    private static RSyntaxTextArea createTextArea() {
        var editor = new RSyntaxTextArea();
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
        editor.setCodeFoldingEnabled(true);

        editor.setBackground(UIManager.getColor("TextArea.background"));
        editor.setForeground(UIManager.getColor("TextArea.foreground"));
        editor.setCaretColor(UIManager.getColor("TextArea.caretForeground"));
        editor.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
        editor.setCurrentLineHighlightColor(UIManager.getColor("TextArea.inactiveBackground"));
        editor.setFont(UIManager.getFont("TextArea.font"));

        var scheme = editor.getSyntaxScheme();
        scheme.getStyle(Token.COMMENT_EOL).foreground = Color.decode("#808080");
        scheme.getStyle(Token.COMMENT_MULTILINE).foreground = Color.decode("#808080");
        scheme.getStyle(Token.RESERVED_WORD).foreground = Color.decode("#CC6C1D");
        scheme.getStyle(Token.FUNCTION).foreground = Color.decode("#A7EC21");
        scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.decode("#CC6C1D");
        scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = Color.decode("#6897BB");
        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).foreground = Color.decode("#6897BB");
        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE ).foreground = Color.decode("#17C6A3");
        scheme.getStyle(Token.LITERAL_CHAR).foreground = Color.decode("#17C6A3");
        scheme.getStyle(Token.LITERAL_BACKQUOTE).foreground = Color.decode("#17C6A3");
        scheme.getStyle(Token.DATA_TYPE).foreground = Color.decode("#CC6C1D");
        scheme.getStyle(Token.VARIABLE).foreground = Color.decode("#66E1F8");
        scheme.getStyle(Token.IDENTIFIER).foreground = Color.decode("#66E1F8");
        scheme.getStyle(Token.SEPARATOR).foreground = UIManager.getColor("TextArea.foreground");
        scheme.getStyle(Token.OPERATOR).foreground = UIManager.getColor("TextArea.foreground");
        scheme.getStyle(Token.ERROR_IDENTIFIER).foreground = Color.decode("#9c0303");;
        scheme.getStyle(Token.ERROR_NUMBER_FORMAT).foreground = Color.decode("#9c0303");;
        scheme.getStyle(Token.ERROR_STRING_DOUBLE).foreground = Color.decode("#9c0303");;
        scheme.getStyle(Token.ERROR_CHAR).foreground = Color.decode("#9c0303");;

        return editor;
    }

    private void createProvider(){
//        var staticProvider = createStaticProvider();
        var dynamicProvider = createDynamicProvider();

//        var combinedProvider = new DefaultCompletionProvider() {
//            @Override
//            public java.util.List<Completion> getCompletions(JTextComponent comp) {
//                java.util.List<Completion> completions = new ArrayList<>();
//                completions.addAll(staticProvider.getCompletions(comp));
//                completions.addAll(dynamicProvider.getCompletions(comp));
//                return completions;
//            }
//
//            @Override
//            public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent comp) {
//                return new ArrayList<>();
//            }
//        };

//        var ac = new AutoCompletion(combinedProvider);
        var ac = new AutoCompletion(dynamicProvider);
        ac.setParameterAssistanceEnabled(true);
        ac.install((RSyntaxTextArea) getViewport().getView());

        this.provider = dynamicProvider;
    }

    private CompletionProvider createStaticProvider(){
        var provider = new DefaultCompletionProvider();
        provider.addCompletion(new BasicCompletion(provider, "world"));
        provider.addCompletion(new BasicCompletion(provider, "player"));
        provider.addCompletion(new BasicCompletion(provider, "resources"));
        return provider;
    }

    private DefaultCompletionProvider createDynamicProvider(){
        return new DefaultCompletionProvider() {

            private static final String[] KEYWORDS = {
                    "and", "break", "do", "else", "elseif",
                    "end", "false", "for", "function", "if",
                    "in", "local", "nil", "not", "or",
                    "repeat", "return", "then", "true", "until", "while" };

            @Override
            public List<Completion> getCompletions(JTextComponent comp) {
                Map<String, Integer> tempUsageCounts = new HashMap<>();
                String text = ((RSyntaxTextArea) comp).getText();
                text = removeComments(text);

//                var regex = "\\b([a-zA-Z_]\\w*)\\b(?!\\s*\\()";
                var regex = "\\b(\\w+)\\s*=\\s*[^=\\s]";
                var pattern = Pattern.compile(regex);
                var matcher = pattern.matcher(text);

                while (matcher.find()) {
                    var variable = matcher.group(1);
                    if(Arrays.stream(KEYWORDS).noneMatch(variable::equals))
                        tempUsageCounts.merge(variable, 1, Integer::sum);
                }

                regex = "(?:\\w+\\.)?\\b(\\w+)\\s*\\(";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(text);

                while (matcher.find()) {
                    var variable = matcher.group(1);
                    if(Arrays.stream(KEYWORDS).noneMatch(variable::equals))
                        tempUsageCounts.merge(variable + "()", 1, Integer::sum);
                }

                // Filter completions by already entered text and sort by usage count
                var enteredText = getAlreadyEnteredText(comp).toLowerCase();
                if(tempUsageCounts.getOrDefault(enteredText, 0) == 1)
                    tempUsageCounts.remove(enteredText);
                var resultList = new ArrayList<>(tempUsageCounts.entrySet().stream()
                        .filter(entry -> entry.getKey().toLowerCase().startsWith(enteredText))
                        //.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .map(entry -> (Completion) new BasicCompletion(this, entry.getKey())).toList());

                resultList.addAll(super.getCompletions(comp));

                return resultList;
            }

//            @Override
//            public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent comp) {
//                Map<String, Integer> tempUsageCounts = new HashMap<>();
//                String text = ((RSyntaxTextArea) comp).getText();
//                text = removeComments(text);
//
//                var regex = "\\b([a-zA-Z_]\\w*)(\\b\\s*\\([^)]*\\))";
//                var pattern = Pattern.compile(regex);
//                var matcher = pattern.matcher(text);
//
//                while (matcher.find()) {
//                    var funcName = matcher.group(1);
//                    var params = matcher.group(2);
//
//                    params = renameParameters(params);
//
//                    tempUsageCounts.merge(funcName + params, 1, Integer::sum);
//                }
//
//                var enteredText = getAlreadyEnteredText(comp).toLowerCase();
//                if(tempUsageCounts.getOrDefault(enteredText, 0) == 1)
//                    tempUsageCounts.remove(enteredText);
//                return tempUsageCounts.entrySet().stream()
//                        .filter(entry -> entry.getKey().toLowerCase().startsWith(enteredText))
//                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
//                        .map(entry -> {
//                            var name = entry.getKey();
//                                return (ParameterizedCompletion) new TemplateCompletion(this, null,
//                                        name.substring(1)
//                                                .replaceAll("\\$\\{i}", "arg")
//                                                .replace("${cursor}", ""),
//                                        name.substring(1));
//                        }).toList();
//            }

            private static String removeComments(String script) {
                script = script.replaceAll("--[^\\[][^\\n]*", "");
                script = script.replaceAll("--\\[{2}[^]]*]{2}", "");

                return script;
            }

//            private static String renameParameters(String parameters) {
//                String paramRegex = "\"(?:[^\"]|\\\\\")*\"|'(?:[^']|\\\\')*'";
//                Pattern paramPattern = Pattern.compile(paramRegex);
//                Matcher paramMatcher = paramPattern.matcher(parameters);
//
//                StringBuffer cleanParams = new StringBuffer();
//                int lastEnd = 0;
//
//                while (paramMatcher.find()) {
//                    cleanParams.append(parameters, lastEnd, paramMatcher.start());
//                    cleanParams.append("\\${i}");
//                    lastEnd = paramMatcher.end();
//                }
//                cleanParams.append(parameters.substring(lastEnd));
//
//                String remainingParams = cleanParams.toString().replaceAll("[^,]+", "\\${i}");
//
//                return "(" + remainingParams + ")${cursor}";
//            }
        };
    }

    public void resetParams(){
        for(var comp: completions)
            provider.removeCompletion(comp);
        completions.clear();
    }

    public void addParams(String... params){
        var completions = Arrays.stream(params).map(p -> (Completion) new BasicCompletion(provider, p)).toList();
        this.completions.addAll(completions);
        provider.addCompletions(completions);
    }

    public RSyntaxTextArea getTextArea(){
        return (RSyntaxTextArea) getViewport().getView();
    }

}
