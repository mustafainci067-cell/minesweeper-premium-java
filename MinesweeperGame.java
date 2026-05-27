import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ProGameRecord implements Comparable<ProGameRecord> {
    private String name, diff;
    private int score, time;
    public ProGameRecord(String name, int score, int time, String diff) {
        this.name = name; this.score = score; this.time = time; this.diff = diff;
    }
    public String getName() { return name; }
    public int getScore() { return score; }
    public String getDiff() { return diff; }
    public int compareTo(ProGameRecord d) { return Integer.compare(d.score, this.score); }
}

class ProScoreManager {
    private ArrayList<ProGameRecord> list = new ArrayList<>();
    private String f = "scores_pro.txt";
    public ProScoreManager() { readScores(); }
    public void readScores() {
        list.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] p = l.split(",");
                if (p.length == 4) list.add(new ProGameRecord(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), p[3]));
            }
        } catch (Exception e) {}
    }
    public void writeScore(ProGameRecord k) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
            bw.write(k.getName() + "," + k.getScore() + "," + k.time + "," + k.getDiff());
            bw.newLine();
        } catch (Exception e) {}
        readScores();
    }
    public String getTop10(String diff) {
        List<ProGameRecord> sec = new ArrayList<>();
        for (ProGameRecord k : list) if (k.getDiff().equals(diff)) sec.add(k);
        Collections.sort(sec);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(10, sec.size()); i++)
            sb.append(i + 1).append(". ").append(sec.get(i).getName()).append(" - ").append(sec.get(i).getScore()).append(" P\n");
        return sb.length() == 0 ? "No scores!" : sb.toString();
    }
}

class ProCell {
    int r, c, k;
    boolean m, a, b;
    JButton btn;
    public ProCell(int r, int c) {
        this.r = r; this.c = c;
        this.btn = new JButton();
        this.btn.setName("cell");
        this.btn.setFont(new Font("Arial", Font.BOLD, 14));
        this.btn.setMargin(new Insets(0,0,0,0));
        this.btn.setFocusPainted(false);
    }
}

public class MinesweeperGame extends JFrame {
    private boolean isDark = false;
    private final Color colorGreen = new Color(46, 204, 113);
    private JToggleButton btnTheme;
    private ProScoreManager sm = new ProScoreManager();
    private String playerName = "";
    private CardLayout cl = new CardLayout();
    private JPanel pnlMain = new JPanel(cl);
    private int R, C, M, sec = 0, revealedCount = 0, score = 0;
    private String diffName;
    private ProCell[][] board;
    private Timer tmr;
    private JLabel lblStatus, lblTime;
    private JPanel pnlGame;
    private boolean isOver = false;

    public MinesweeperGame() {
        setTitle("Minesweeper Premium");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnTheme = new JToggleButton("🌙 Dark Mode");
        btnTheme.setFocusPainted(false);
        btnTheme.addActionListener(e -> {
            isDark = btnTheme.isSelected();
            btnTheme.setText(isDark ? "☀️ Light Mode" : "🌙 Dark Mode");
            applyTheme();
        });
        pnlTop.add(btnTheme);
        add(pnlTop, BorderLayout.NORTH);

        pnlMain.add(screenLogin(), "L");
        pnlMain.add(screenMenu(), "M");
        pnlGame = new JPanel(new BorderLayout());
        pnlMain.add(pnlGame, "G");

        add(pnlMain, BorderLayout.CENTER);
        applyTheme();
        pack(); setLocationRelativeTo(null); setVisible(true);
    }

    private JPanel screenLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(500, 400));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,10,10,10);
        g.gridy = 0; p.add(new JLabel("MINESWEEPER"), g);
        g.gridy = 1; p.add(new JLabel("Name:"), g);
        JTextField txt = new JTextField(15);
        txt.setHorizontalAlignment(JTextField.CENTER);
        g.gridy = 2; p.add(txt, g);
        JButton btn = new JButton("CONTINUE");
        btn.setName("special");
        btn.addActionListener(e -> {
            if (txt.getText().trim().isEmpty()) return;
            playerName = txt.getText().trim();
            cl.show(pnlMain, "M");
        });
        g.gridy = 3; p.add(btn, g);
        return p;
    }

    private JPanel screenMenu() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(new EmptyBorder(20,20,20,20));
        p.add(new JLabel("Select Difficulty:"), BorderLayout.NORTH);
        
        JPanel pZ = new JPanel(new GridLayout(5,1,5,5));
        pZ.setBorder(BorderFactory.createTitledBorder("Difficulty Levels"));
        String[] dNames = {"Easy", "Medium", "Hard", "Pro", "Extreme"};
        int[][] dVals = {{9,9,10}, {16,16,40}, {20,20,80}, {25,25,120}, {30,30,200}};
        ButtonGroup bg = new ButtonGroup();
        JRadioButton[] rad = new JRadioButton[5];
        
        JTextArea txtS = new JTextArea();
        txtS.setEditable(false);
        JScrollPane scr = new JScrollPane(txtS);
        scr.setPreferredSize(new Dimension(200,200));
        scr.setBorder(BorderFactory.createTitledBorder("Top 10"));

        for (int i = 0; i < 5; i++) {
            rad[i] = new JRadioButton(dNames[i]);
            bg.add(rad[i]); pZ.add(rad[i]);
            int idx = i;
            rad[i].addActionListener(e -> txtS.setText(sm.getTop10(dNames[idx])));
        }
        rad[0].setSelected(true); txtS.setText(sm.getTop10("Easy"));

        p.add(pZ, BorderLayout.CENTER); p.add(scr, BorderLayout.EAST);
        
        JPanel pA = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btn = new JButton("START GAME");
        btn.setName("special");
        btn.addActionListener(e -> {
            for (int i=0; i<5; i++) {
                if (rad[i].isSelected()) { setupGame(dVals[i][0], dVals[i][1], dVals[i][2], dNames[i]); break; }
            }
        });
        pA.add(btn); p.add(pA, BorderLayout.SOUTH);
        return p;
    }

    private void setupGame(int r, int c, int m, String diff) {
        this.R = r; this.C = c; this.M = m; this.diffName = diff;
        sec = 0; revealedCount = 0; score = 0; isOver = false;
        board = new ProCell[R][C];
        pnlGame.removeAll();

        JPanel u = new JPanel(new GridLayout(1,2));
        lblStatus = new JLabel(" Score: 0"); lblTime = new JLabel("Time: 0s ", SwingConstants.RIGHT);
        u.add(lblStatus); u.add(lblTime);
        pnlGame.add(u, BorderLayout.NORTH);

        JPanel pt = new JPanel(new GridLayout(R,C));
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) { // SUTUN -> C yapıldı genel uyum için
                board[i][j] = new ProCell(i, j);
                ProCell h = board[i][j];
                h.btn.setPreferredSize(new Dimension(35,35));
                h.btn.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        if(isOver) return;
                        if(SwingUtilities.isRightMouseButton(e)) toggleFlag(h);
                        else if(h.a) revealNeighbors(h);
                        else revealCell(h.r, h.c);
                    }
                });
                pt.add(h.btn);
            }
        }
        pnlGame.add(pt, BorderLayout.CENTER);

        if (tmr != null) tmr.stop();
        tmr = new Timer(1000, e -> lblTime.setText("Time: " + (++sec) + "s "));

        int n = 0;
        while(n < M) {
            int rr = (int)(Math.random()*R), cc = (int)(Math.random()*C);
            if(!board[rr][cc].m) { board[rr][cc].m = true; n++; }
        }
        for (int i=0; i<R; i++) for (int j=0; j<C; j++) {
            if(board[i][j].m) continue;
            int s=0;
            for(int x=-1; x<=1; x++) for(int y=-1; y<=1; y++) {
                int nr = i+x, nc = j+y;
                if(nr>=0 && nr<R && nc>=0 && nc<C && board[nr][nc].m) s++;
            }
            board[i][j].k = s;
        }
        applyTheme();
        cl.show(pnlMain, "G"); pack(); setLocationRelativeTo(null);
    }

    private void toggleFlag(ProCell h) {
        if(h.a) return;
        h.b = !h.b;
        h.btn.setText(h.b ? "F" : "");
        h.btn.setForeground(h.b ? (isDark ? colorGreen : Color.RED) : Color.BLACK);
    }

    private void revealCell(int r, int c) {
        if(r<0 || r>=R || c<0 || c>=C) return;
        ProCell h = board[r][c];
        if(h.a || h.b) return;
        if(!tmr.isRunning()) tmr.start();

        h.a = true; h.btn.setEnabled(false);
        h.btn.setBackground(isDark ? new Color(25,25,25) : new Color(180,180,180));
        revealedCount++;

        if(h.m) { endGame(false); return; }
        score += (M*2); lblStatus.setText(" Score: " + score);

        if(h.k > 0) {
            h.btn.setText(String.valueOf(h.k));
            h.btn.setForeground(isDark ? Color.CYAN : Color.BLUE);
        } else {
            for(int i=-1; i<=1; i++) for(int j=-1; j<=1; j++) revealCell(r+i, c+j);
        }
        if(revealedCount == (R*C)-M) endGame(true);
    }

    private void revealNeighbors(ProCell h) {
            int s = 0;
            for(int i=-1; i<=1; i++) for(int j=-1; j<=1; j++) {
                int nr = h.r+i, nc = h.c+j;
                if(nr>=0 && nr<R && nc>=0 && nc<C && board[nr][nc].b) s++;
            }
            if(s == h.k) {
                for(int i=-1; i<=1; i++) for(int j=-1; j<=1; j++) revealCell(h.r+i, h.c+j);
            }
    }

    private void endGame(boolean isWin) {
        isOver = true; tmr.stop();
        if(isWin) {
            sm.writeScore(new ProGameRecord(playerName, score + Math.max(0, 1000-sec), sec, diffName));
            JOptionPane.showMessageDialog(this, "You Won!");
        } else {
            for(int i=0; i<R; i++) for(int j=0; j<C; j++) {
                if(board[i][j].m) { board[i][j].btn.setText("*"); board[i][j].btn.setBackground(Color.RED); board[i][j].btn.setForeground(Color.WHITE); }
            }
            JOptionPane.showMessageDialog(this, "You Lost!");
        }
        cl.show(pnlMain, "M"); pack(); setLocationRelativeTo(null);
    }

    private void applyTheme() {
        Color bg = isDark ? new Color(43,43,43) : new Color(240,240,240);
        UIManager.put("OptionPane.background", bg);
        UIManager.put("Panel.background", bg);
        UIManager.put("OptionPane.messageForeground", isDark ? Color.WHITE : Color.BLACK);
        updateThemeRec(getContentPane());
        if(board != null) {
            for(int i=0; i<R; i++) for(int j=0; j<C; j++) {
                ProCell h = board[i][j];
                if(h.a) {
                    h.btn.setBackground(isDark ? new Color(25,25,25) : new Color(180,180,180));
                    if(h.m) { h.btn.setBackground(Color.RED); h.btn.setForeground(Color.WHITE); }
                    else h.btn.setForeground(isDark ? Color.CYAN : Color.BLUE);
                } else {
                    h.btn.setBackground(isDark ? new Color(75,80,85) : new Color(220,220,220));
                    if(h.b) h.btn.setForeground(isDark ? colorGreen : Color.RED);
                }
            }
        }
        repaint();
    }

    private void updateThemeRec(Container c) {
        Color bg = isDark ? new Color(43,43,43) : new Color(240,240,240);
        Color fg = isDark ? Color.WHITE : Color.BLACK;
        c.setBackground(bg);
        for(Component x : c.getComponents()) {
            if (x == btnTheme) {
                x.setBackground(isDark ? new Color(70,70,70) : new Color(200,200,200));
                x.setForeground(fg);
            } else if(x instanceof JButton) {
                JButton b = (JButton) x;
                if("cell".equals(b.getName())) continue;
                if("special".equals(b.getName())) { b.setBackground(colorGreen); b.setForeground(Color.BLACK); }
            } else if(x instanceof JLabel) { ((JLabel)x).setForeground(fg);
            } else if(x instanceof JRadioButton) { x.setBackground(bg); x.setForeground(fg);
            } else if(x instanceof JTextComponent) { x.setBackground(isDark ? new Color(30,30,30) : Color.WHITE); x.setForeground(isDark ? colorGreen : new Color(34,139,34)); ((JTextComponent)x).setCaretColor(fg);
            } else if(x instanceof JScrollPane) { x.setBackground(bg); ((JScrollPane)x).getViewport().setBackground(bg); }
            
            if(x instanceof JComponent && ((JComponent)x).getBorder() instanceof TitledBorder) {
                ((TitledBorder)((JComponent)x).getBorder()).setTitleColor(isDark ? colorGreen : new Color(34,139,34)); x.repaint();
            }
            if(x instanceof Container) updateThemeRec((Container)x);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MinesweeperGame());
    }
}