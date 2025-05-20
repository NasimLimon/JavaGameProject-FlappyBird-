import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int PIPE_WIDTH = 30;
    private final int PIPE_GAP = 200;

    private Image backgroundImg;
    private Image birdImg;
    private Image pipeUpImg;
    private Image pipeDownImg;

    private Timer gameTimer;
    private ArrayList<Rectangle> pipes;
    private ArrayList<Rectangle> scoredPipes;
    private int birdX = WIDTH / 4;
    private int birdY = HEIGHT / 2;
    private int birdWidth = 34;
    private int birdHeight = 24;
    private int velocity = 0;
    private int gravity = 1;
    private boolean gameOver = false;
    private int score = 0;
    private Random rand;

    private Clip backgroundClip;
    private Clip dieClip;
    private boolean gameStarted = false;
    private JButton startButton;

    public FlappyBird() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon(getClass().getResource("Background.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("Bird.png")).getImage();
        pipeUpImg = new ImageIcon(getClass().getResource("pipe2up.png")).getImage();
        pipeDownImg = new ImageIcon(getClass().getResource("pipe2down.png")).getImage();

        pipes = new ArrayList<>();
        scoredPipes = new ArrayList<>(); 
        rand = new Random();

        setLayout(null);
        startButton = new JButton("Start Game");
        startButton.setBounds(WIDTH / 2 - 50, HEIGHT / 2 - 25, 100, 50);
        startButton.addActionListener(e -> startGame());
        add(startButton);
    }

    private void startGame() {
        gameStarted = true;
        startButton.setVisible(false);
        pipes.clear();
        scoredPipes.clear();
        addPipe();
        addPipe();
        playBackgroundMusic("/Backsong.wav");
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void playBackgroundMusic(String filePath) {
        try {
            if (backgroundClip != null && backgroundClip.isOpen()) {
                backgroundClip.stop();
                backgroundClip.close();
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioInputStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playDieSound(String filePath) {
        try {
            if (dieClip != null && dieClip.isRunning()) {
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            dieClip = AudioSystem.getClip();
            dieClip.open(audioInputStream);
            dieClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPipe() {
        int pipeHeight = 100 + rand.nextInt(200);
        int yPosTop = pipeHeight - 512;
        int yPosBottom = pipeHeight + PIPE_GAP;

        pipes.add(new Rectangle(WIDTH, yPosTop, PIPE_WIDTH, 512));
        pipes.add(new Rectangle(WIDTH, yPosBottom, PIPE_WIDTH, 512));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, WIDTH, HEIGHT, null);

        if (!gameStarted) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Flappy Bird", WIDTH / 2 - 100, HEIGHT / 2 - 100);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            return;
        }

        for (Rectangle pipe : pipes) {
            if (pipe.y < 0) {
                g.drawImage(pipeUpImg, pipe.x, pipe.y, pipe.width, pipe.height, null);
            } else {
                g.drawImage(pipeDownImg, pipe.x, pipe.y, pipe.width, pipe.height, null);
            }
        }

        g.drawImage(birdImg, birdX, birdY, birdWidth, birdHeight, null);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 10, 30);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 50);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Final Score: " + score, WIDTH / 2 - 70, HEIGHT / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            moveBird();
            movePipes();
            checkCollision();
        }
        repaint();
    }

    private void moveBird() {
        velocity += gravity;
        birdY += velocity;

        if (birdY >= HEIGHT - birdHeight) {
            if (!gameOver) {
                gameOver = true;
                playDieSound("/Die.wav");
            }
        }
    }

    private void movePipes() {
        for (int i = 0; i < pipes.size(); i++) {
            Rectangle pipe = pipes.get(i);
            pipe.x -= 4;

            if (pipe.x + PIPE_WIDTH < 0) {
                pipes.remove(pipe);
                if (pipe.y > 0) {
                    addPipe();
                }
                i--;
            }
        }
    }

    private void checkCollision() {
        Rectangle birdRect = new Rectangle(birdX, birdY, birdWidth, birdHeight);

        for (Rectangle pipe : pipes) {
            if (pipe.intersects(birdRect)) {
                if (!gameOver) {
                    gameOver = true;
                    playDieSound("/Die.wav");
                }
            }

        
            if (!gameOver && pipe.y > 0 && pipe.x + PIPE_WIDTH < birdX && !scoredPipes.contains(pipe)) {
                score++;
                scoredPipes.add(pipe);
            }
        }

        if (birdY <= 0 || birdY >= HEIGHT - birdHeight) {
            if (!gameOver) {
                gameOver = true;
                playDieSound("/Die.wav");
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE && gameStarted) {
            if (gameOver) {
                resetGame();
            }
        } else if (key == KeyEvent.VK_UP && !gameOver) {
            velocity = -10;
        }
    }

    private void resetGame() {
        birdY = HEIGHT / 2;
        pipes.clear();
        scoredPipes.clear(); 
        addPipe();
        addPipe();
        velocity = 0;
        score = 0;
        gameOver = false;

        if (backgroundClip != null) {
            backgroundClip.setFramePosition(0);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
