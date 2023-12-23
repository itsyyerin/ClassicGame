import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ExtendedGame extends JFrame {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final int PLAYER_SIZE = 20;
    private static final int LARGE_RECTANGLE_SIZE = 30;
    private static final int PLAYER_SPEED = 5;
    private static final int GAME_DURATION = 10; // 10 seconds

    private int playerX = WIDTH / 2 - PLAYER_SIZE / 2;
    private int playerY = HEIGHT - PLAYER_SIZE - 20;

    private int largeRectangleX;
    private int largeRectangleY;

    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveUp;
    private boolean moveDown;
    

    private boolean gameRunning = true;
    private boolean gameFinished = false;

    private long startTime;

    private JLabel timeLabel;

    public ExtendedGame() {
        setTitle("Extended Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });

        setFocusable(true);

        // 큰 사각형의 위치를 랜덤으로 설정
        generateRandomLargeRectanglePosition();

        startTime = System.currentTimeMillis();

        // 10초 동안 게임이 진행되도록 타이머 설정
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int remainingTime = (int) ((GAME_DURATION * 1000 - elapsedTime) / 1000);
                if (remainingTime <= 0) {
                    gameRunning = false;
                    if (!gameFinished) {
                        showResultDialog(false); // 실패
                    }
                } else {
                    timeLabel.setText("Time Remaining: " + remainingTime + " seconds");
                }
            }
        });
        timer.setInitialDelay(0); // 처음 딜레이를 0으로 설정하여 바로 시작
        timer.start();

        // 남은 시간을 표시할 레이블 추가
        timeLabel = new JLabel("Time Remaining: " + GAME_DURATION + " seconds");
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER); // 가운데 정렬
        timeLabel.setBounds(0, HEIGHT - 30, WIDTH, 20); // 하단에 위치하도록 조절
        add(timeLabel);

        // 게임 루프를 시작하는 부분 추가
        startGameLoop();
    }

    private void handleKeyPress(KeyEvent e) {
        if (!gameRunning) {
            return;
        }

        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT) {
            moveLeft = true;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            moveRight = true;
        } else if (keyCode == KeyEvent.VK_UP) {
            moveUp = true;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            moveDown = true;
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT) {
            moveLeft = false;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            moveRight = false;
        } else if (keyCode == KeyEvent.VK_UP) {
            moveUp = false;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            moveDown = false;
        }
    }

    private void movePlayer() {
        if (!gameRunning) {
            return;
        }

        if (moveLeft && playerX > 0) {
            playerX -= PLAYER_SPEED;
        } else if (moveRight && playerX < WIDTH - PLAYER_SIZE) {
            playerX += PLAYER_SPEED;
        }

        if (moveUp && playerY > 0) {
            playerY -= PLAYER_SPEED;
        } else if (moveDown && playerY < HEIGHT - PLAYER_SIZE) {
            playerY += PLAYER_SPEED;
        }

        checkCollision(); // 충돌 체크
    }

    private void checkCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
        Rectangle largeRect = new Rectangle(largeRectangleX, largeRectangleY, LARGE_RECTANGLE_SIZE, LARGE_RECTANGLE_SIZE);

        if (playerRect.intersects(largeRect)) {
            if (largeRect.contains(playerRect)) {
                gameRunning = false;
                gameFinished = true;
                showResultDialog(true); // 성공
            }
        }

        // 그래픽 업데이트를 EDT에서 실행
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    private void generateRandomLargeRectanglePosition() {
        // 빨간 사각형이 창의 크기를 벗어나지 않도록 조절
        int maxX = WIDTH - LARGE_RECTANGLE_SIZE;
        int maxY = HEIGHT - LARGE_RECTANGLE_SIZE;

        largeRectangleX = (int) (Math.random() * maxX);
        largeRectangleY = (int) (Math.random() * maxY);
    }

    private void showResultDialog(boolean success) {
        if (success) {
            JOptionPane.showMessageDialog(this, "성공! 10초 안에 성공적으로 도달했습니다.");
        } else {
            JOptionPane.showMessageDialog(this, "실패! 10초 동안 목표 지점에 도달하지 못했습니다.");
        }
        System.exit(0);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);

        g.setColor(Color.RED);
        g.fillRect(largeRectangleX, largeRectangleY, LARGE_RECTANGLE_SIZE, LARGE_RECTANGLE_SIZE);
    }

    private void startGameLoop() {
        // 게임 루프를 위한 스레드 생성 및 시작
        Thread gameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (gameRunning) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 플레이어 이동 및 그래픽 업데이트를 EDT에서 실행
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            movePlayer();
                            repaint();
                        }
                    });
                }

                if (!gameFinished) {
                    showResultDialog(false); // 실패
                }
            }
        });

        gameThread.start(); // 스레드 시작
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ExtendedGame game = new ExtendedGame();
                game.setVisible(true);
            }
        });
    }
}

