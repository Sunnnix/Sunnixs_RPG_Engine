package de.sunnix.engine.debug.profiler;

import de.sunnix.engine.Core;
import de.sunnix.engine.debug.GameLogger;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Profiler {

    private static int precision = Core.getWindow_targetFPS() * 30;

    private static List<Long> totalData = new ArrayList<>();
    private static Map<String, List<Long>> data = new HashMap<>();

    public static void profileTotal(long time) {
        var list = totalData;
        editList(() -> {
            list.add(time);
            var diff = list.size() - precision;
            if(diff > 1){
                list.removeAll(list.subList(0,diff));
            } else if(diff > 0)
                list.remove(0);
        });
    }

    public static void profile(String id, long time){
        editList(() -> {
            var list = data.computeIfAbsent(id, s -> new ArrayList<>(precision));
            list.add(time);
            var diff = list.size() - precision;
            if(diff > 1){
                list.removeAll(list.subList(0,diff));
            } else if(diff > 0)
                list.remove(0);
        });
    }

    private static synchronized void editList(Runnable run){
        run.run();
    }

    public static void createWindow(){
        var thread = new Thread(() -> {
            new JFrame() {

                JPanel root;

                Monitor totalTime = new Monitor();
                Map<String, Monitor> monitors = new HashMap<>();

                {
                    setTitle("Profiler");
                    setMinimumSize(new Dimension(234, 42 + 80 * 3));
                    setSize(600, 42 + 80 * 5);
                    root = new JPanel();
                    root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
                    setContentPane(new JScrollPane(root));
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    setVisible(true);
                    observer();

                }

                void observer(){
                    Thread.startVirtualThread(() -> {
                        GameLogger.logI("PofilerObserver", "Profiler Started");
                        try {
                            root.add(totalTime);
                            var wrapper = new Object(){
                                int i = 0;
                            };
                            while (this.isShowing()) {
                                SwingUtilities.invokeLater(() -> {
                                    editList(() -> {
                                        totalTime.setData(totalData);
                                        data.entrySet().forEach(e -> {
                                            monitors.computeIfAbsent(e.getKey(), s -> {
                                                var monitor = (Monitor) root.add(new Monitor(s));
                                                root.revalidate();
                                                return monitor;
                                            }).setData(e.getValue());
                                            if(wrapper.i++ % 82 == 0){
                                                root.removeAll();
                                                root.add(totalTime);
                                                monitors.values().stream().sorted(Comparator.comparing(Monitor::getAvr).reversed()).forEach(root::add);
                                                root.revalidate();
                                                root.repaint();
                                            } else
                                                root.repaint();
                                        });
                                    });
                                });
                                Thread.sleep(33);
                            }
                        } catch (InterruptedException e){
                            GameLogger.logW("PofilerObserver", "Observer closed cause of a exception (" + e + ")");
                        }
                        if(this.isShowing()) {
                            GameLogger.logW("PofilerObserver", "Profiler Closing cause Thread ended and window is showing");
                            this.dispose();
                        }
                        GameLogger.logI("PofilerObserver", "Profiler Stopped");
                    });
                }

            };
        }, "ProfilerWindow");
        thread.setDaemon(true);
        thread.start();
    }

    private static class Monitor extends JPanel {

        static int nextPos;

        static Color textColor = new Color(100, 255, 0);
        static Color[] colors = {
                new Color(0, 0, 85),
                new Color(150, 25, 100),
                new Color(150, 100, 0),
                new Color(0, 120, 120),
                new Color(70, 0, 150)
        };

        List<Long> data = new ArrayList<>();
        final String name;

        Color lineColor;
        Color backColor;

        boolean showFPS;

        Monitor(){
            this.name = "Total Time";
            lineColor = Color.WHITE;
            backColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 100);
            setBackground(Color.DARK_GRAY);
            setMaximumSize(new Dimension(12000, 80));
            setMinimumSize(new Dimension(200, 80));
            setPreferredSize(new Dimension(200, 80));
            showFPS = true;
        }

        Monitor(String name){
            this.name = name;
            lineColor = colors[nextPos++ % colors.length];
            backColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 100);
            setBackground(Color.DARK_GRAY);
            setMaximumSize(new Dimension(12000, 80));
            setMinimumSize(new Dimension(200, 80));
            setPreferredSize(new Dimension(200, 80));
        }

        void setData(List<Long> data) {
            this.data.clear();
            this.data.addAll(data);
        }

        double getAvr(){
            return data.stream().mapToLong(l -> l).average().orElse(0);
        }

        float getAvrMS(){
            return (int)(getAvr() / 1000) / 1000f;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            var pixelList = new ArrayList<>(data.subList(Math.max(0, data.size() - getWidth()), data.size()));
            var twoThird = 1f / 3 * 2;
            var twoThirdPixels = twoThird * getHeight();
            var avr = (long)pixelList.stream().mapToLong(l -> l).average().orElse(0);
            var msPerPixel = twoThirdPixels / avr;

            var poly = new Polygon();
            poly.npoints = pixelList.size() + 2;
            poly.xpoints = new int[pixelList.size() + 2];
            poly.ypoints = new int[pixelList.size() + 2];
            for (int i = 0; i < pixelList.size(); i++) {
                var p = pixelList.get(pixelList.size() - 1 - i);
                var height = (int)(p * msPerPixel);
                poly.xpoints[i] = getWidth() - i;
                poly.ypoints[i] = getHeight() - height;
            }
            poly.xpoints[pixelList.size() + 1] = getWidth();
            poly.xpoints[pixelList.size()] = getWidth() - pixelList.size();
            poly.ypoints[pixelList.size() + 1] = getHeight();
            poly.ypoints[pixelList.size()] = getHeight();

            g.setColor(backColor);
            g.fillPolygon(poly);
            g.setColor(lineColor);
            g.drawPolygon(poly);
            g.setColor(textColor);
            g.drawLine(0, getHeight() - (int)twoThirdPixels, getWidth(), getHeight() - (int)twoThirdPixels);
            g.setFont(new Font("Comic Sans", Font.BOLD, 20));
            g.drawString(name, 5, 20);
            String s;
            if(showFPS) {
                var ms = (int) (avr / 1000) / 1000f;
                s = String.format("%s FPS | %.3f ms", (int)(1 / (ms / 1000)), ms);
            } else
                s = String.format("%.3f ms", (int)(avr / 1000) / 1000f);
            g.drawString(s, getWidth() - 5 - g.getFontMetrics().stringWidth(s), 20);
        }
    }

}
