package com.mycompany.furnituredesignapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.geom.AffineTransform;

public class FurnitureDesignApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

class MainFrame extends JFrame {
    private DesignPanel designPanel;
    private RoomConfigPanel configPanel;
    private FurnitureSelectionPanel furniturePanel;
    private JButton toggle3DButton;
    private JButton deleteButton;
    private JButton rotateButton;
    private JButton increaseSizeButton;
    private JButton decreaseSizeButton;
    private JButton clearButton;
    private boolean is3DView = false;

    public MainFrame() {
        setTitle("Furniture Design Application");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Design");
        JMenuItem loadItem = new JMenuItem("Load Design");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> saveDesign());
        loadItem.addActionListener(e -> loadDesign());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        configPanel = new RoomConfigPanel();
        designPanel = new DesignPanel();
        furniturePanel = new FurnitureSelectionPanel(designPanel);

        toggle3DButton = new JButton("Switch to 3D View");
        toggle3DButton.addActionListener(e -> toggleView());

        deleteButton = new JButton("Delete Selected (Del)");
        deleteButton.addActionListener(e -> designPanel.deleteSelectedFurniture());
        deleteButton.setEnabled(false);

        rotateButton = new JButton("Rotate (R)");
        rotateButton.addActionListener(e -> designPanel.rotateSelectedFurniture());
        rotateButton.setEnabled(false);

        increaseSizeButton = new JButton("Increase Size (+)");
        increaseSizeButton.addActionListener(e -> designPanel.resizeSelectedFurniture(1.1));
        increaseSizeButton.setEnabled(false);

        decreaseSizeButton = new JButton("Decrease Size (-)");
        decreaseSizeButton.addActionListener(e -> designPanel.resizeSelectedFurniture(0.9));
        decreaseSizeButton.setEnabled(false);

        clearButton = new JButton("Clear Design");
        clearButton.addActionListener(e -> clearDesign());

        designPanel.addSelectionListener(selected -> {
            deleteButton.setEnabled(selected != null);
            rotateButton.setEnabled(selected != null);
            increaseSizeButton.setEnabled(selected != null);
            decreaseSizeButton.setEnabled(selected != null);
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(configPanel, BorderLayout.NORTH);
        leftPanel.add(furniturePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.add(toggle3DButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(rotateButton);
        buttonPanel.add(increaseSizeButton);
        buttonPanel.add(decreaseSizeButton);
        buttonPanel.add(clearButton);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(designPanel, BorderLayout.CENTER);

        configPanel.setConfigListener(designPanel);
    }

    private void toggleView() {
        is3DView = !is3DView;
        if (is3DView) {
            toggle3DButton.setText("Switch to 2D View");
            JOptionPane.showMessageDialog(this, "3D view would be implemented with Java3D/JOGL");
        } else {
            toggle3DButton.setText("Switch to 3D View");
            designPanel.repaint();
        }
    }

    private void saveDesign() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(fileChooser.getSelectedFile()))) {
                oos.writeObject(designPanel.getDesignData());
                JOptionPane.showMessageDialog(this, "Design saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving design: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDesign() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(fileChooser.getSelectedFile()))) {
                DesignData data = (DesignData) ois.readObject();
                designPanel.setDesignData(data);
                configPanel.setRoomDimensions(data.roomWidth, data.roomHeight);
                JOptionPane.showMessageDialog(this, "Design loaded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading design: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearDesign() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the entire design?",
                "Confirm Clear Design",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            designPanel.clearDesign();
        }
    }
}

class RoomConfigPanel extends JPanel {
    private JSpinner widthSpinner, heightSpinner;
    private JComboBox<String> shapeComboBox;
    private JButton colorButton;
    private Color roomColor = Color.WHITE;
    private RoomConfigListener listener;

    public RoomConfigPanel() {
        setBorder(BorderFactory.createTitledBorder("Room Configuration"));
        setLayout(new GridLayout(0, 2, 5, 5));

        add(new JLabel("Width (m):"));
        widthSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 50.0, 0.5));
        add(widthSpinner);

        add(new JLabel("Height (m):"));
        heightSpinner = new JSpinner(new SpinnerNumberModel(3.0, 1.0, 50.0, 0.5));
        add(heightSpinner);

        add(new JLabel("Shape:"));
        shapeComboBox = new JComboBox<>(new String[]{"Rectangle", "L-Shape", "Square"});
        add(shapeComboBox);

        add(new JLabel("Wall Color:"));
        colorButton = new JButton("Choose");
        colorButton.addActionListener(e -> chooseColor());
        add(colorButton);

        JButton applyButton = new JButton("Apply Configuration");
        applyButton.addActionListener(e -> applyConfig());
        add(applyButton);
    }

    private void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Room Color", roomColor);
        if (newColor != null) {
            roomColor = newColor;
            colorButton.setBackground(roomColor);
        }
    }

    private void applyConfig() {
        if (listener != null) {
            double width = (Double) widthSpinner.getValue();
            double height = (Double) heightSpinner.getValue();
            String shape = (String) shapeComboBox.getSelectedItem();
            listener.onRoomConfigChanged(width, height, shape, roomColor);
        }
    }

    public void setConfigListener(RoomConfigListener listener) {
        this.listener = listener;
    }

    public void setRoomDimensions(double width, double height) {
        widthSpinner.setValue(width);
        heightSpinner.setValue(height);
    }
}

interface RoomConfigListener {
    void onRoomConfigChanged(double width, double height, String shape, Color color);
}

class FurnitureSelectionPanel extends JPanel {
    private DesignPanel designPanel;
    private static final int ICON_SIZE = 32;

    public FurnitureSelectionPanel(DesignPanel designPanel) {
        this.designPanel = designPanel;
        setBorder(BorderFactory.createTitledBorder("Furniture Selection"));
        setLayout(new GridLayout(0, 2, 5, 5));

        addFurnitureButton("Chair");
        addFurnitureButton("Table");
        addFurnitureButton("Sofa");
        addFurnitureButton("Bed");
        addFurnitureButton("Cabinet");
        addFurnitureButton("Lamp");
    }

    private void addFurnitureButton(String name) {
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/" + name.toLowerCase() + ".png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            JButton button = new JButton(name, scaledIcon);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.addActionListener(event -> designPanel.setSelectedFurniture(name));
            add(button);
        } catch (Exception ex) {
            JButton button = new JButton(name);
            button.addActionListener(event -> designPanel.setSelectedFurniture(name));
            add(button);
        }
    }
}

class DesignPanel extends JPanel implements RoomConfigListener {
    private double roomWidth = 5.0;
    private double roomHeight = 3.0;
    private String roomShape = "Rectangle";
    private Color roomColor = Color.WHITE;
    private String selectedFurnitureType = null;
    private ArrayList<FurnitureItem> furnitureItems = new ArrayList<>();
    private Point dragStart = null;
    private FurnitureItem selectedItem = null;
    private FurnitureItem draggedItem = null;
    private ArrayList<SelectionListener> selectionListeners = new ArrayList<>();
    private boolean addingFurniture = false;

    public DesignPanel() {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setBackground(Color.LIGHT_GRAY);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (addingFurniture) {
                        FurnitureItem item = new FurnitureItem(selectedFurnitureType,
                                e.getX(), e.getY(), 80, 60);
                        furnitureItems.add(item);
                        setSelectedItem(item);
                        draggedItem = item;
                        dragStart = e.getPoint();
                        addingFurniture = false;
                        repaint();
                    } else {
                        for (int i = furnitureItems.size() - 1; i >= 0; i--) {
                            FurnitureItem item = furnitureItems.get(i);
                            if (item.contains(e.getPoint())) {
                                setSelectedItem(item);
                                draggedItem = item;
                                dragStart = e.getPoint();
                                furnitureItems.remove(i);
                                furnitureItems.add(item);
                                break;
                            }
                        }
                        if (draggedItem == null) {
                            setSelectedItem(null);
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (draggedItem != null) {
                    int gridSize = 10;
                    int newX = (draggedItem.getX() / gridSize) * gridSize;
                    int newY = (draggedItem.getY() / gridSize) * gridSize;
                    draggedItem.setPosition(newX, newY);
                    draggedItem = null;
                    repaint();
                }
                dragStart = null;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (draggedItem != null && dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    draggedItem.move(dx, dy);
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE && selectedItem != null) {
                    deleteSelectedFurniture();
                } else if (e.getKeyCode() == KeyEvent.VK_R && selectedItem != null) {
                    rotateSelectedFurniture();
                } else if (e.getKeyCode() == KeyEvent.VK_EQUALS && selectedItem != null) {
                    resizeSelectedFurniture(1.1);
                } else if (e.getKeyCode() == KeyEvent.VK_MINUS && selectedItem != null) {
                    resizeSelectedFurniture(0.9);
                }
            }
        });
    }

    public void clearDesign() {
        furnitureItems.clear();
        setSelectedItem(null);
        repaint();
    }

    public void rotateSelectedFurniture() {
        if (selectedItem != null) {
            selectedItem.rotate(90);
            repaint();
        }
    }

    public void resizeSelectedFurniture(double scaleFactor) {
        if (selectedItem != null) {
            selectedItem.resize(scaleFactor);
            repaint();
        }
    }

    private void showContextMenu(int x, int y) {
        for (int i = furnitureItems.size() - 1; i >= 0; i--) {
            FurnitureItem item = furnitureItems.get(i);
            if (item.contains(new Point(x, y))) {
                setSelectedItem(item);

                JPopupMenu popupMenu = new JPopupMenu();
                
                JMenuItem rotateItem = new JMenuItem("Rotate 90°");
                rotateItem.addActionListener(e -> {
                    item.rotate(90);
                    repaint();
                });
                
                JMenuItem increaseSizeItem = new JMenuItem("Increase Size");
                increaseSizeItem.addActionListener(e -> {
                    item.resize(1.1);
                    repaint();
                });
                
                JMenuItem decreaseSizeItem = new JMenuItem("Decrease Size");
                decreaseSizeItem.addActionListener(e -> {
                    item.resize(0.9);
                    repaint();
                });
                
                JMenuItem deleteItem = new JMenuItem("Delete");
                deleteItem.addActionListener(e -> deleteSelectedFurniture());
                
                popupMenu.add(rotateItem);
                popupMenu.add(increaseSizeItem);
                popupMenu.add(decreaseSizeItem);
                popupMenu.addSeparator();
                popupMenu.add(deleteItem);

                popupMenu.show(this, x, y);
                return;
            }
        }
        setSelectedItem(null);
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(listener);
    }

    private void setSelectedItem(FurnitureItem item) {
        this.selectedItem = item;
        for (SelectionListener listener : selectionListeners) {
            listener.onSelectionChanged(item);
        }
        repaint();
    }

    public void setSelectedFurniture(String type) {
        this.selectedFurnitureType = type;
        addingFurniture = true;
        setSelectedItem(null);
    }

    public void deleteSelectedFurniture() {
        if (selectedItem != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this " + selectedItem.getType() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                furnitureItems.remove(selectedItem);
                setSelectedItem(null);
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int baseX = 50;
        int baseY = 50;
        int widthPixels = getRoomWidthPixels();
        int heightPixels = getRoomHeightPixels();
        
        g2d.setColor(roomColor);
        
        switch (roomShape) {
            case "Rectangle":
                g2d.fillRect(baseX, baseY, widthPixels, heightPixels);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(baseX, baseY, widthPixels, heightPixels);
                drawGrid(g2d, baseX, baseY, widthPixels, heightPixels);
                break;
                
            case "L-Shape":
                int mainWidth = widthPixels;
                int mainHeight = heightPixels/2;
                g2d.fillRect(baseX, baseY, mainWidth, mainHeight);
                
                int extensionWidth = widthPixels/2;
                int extensionHeight = heightPixels/2;
                g2d.fillRect(baseX, baseY + mainHeight, extensionWidth, extensionHeight);
                
                g2d.setColor(Color.BLACK);
                g2d.drawRect(baseX, baseY, mainWidth, mainHeight);
                g2d.drawRect(baseX, baseY + mainHeight, extensionWidth, extensionHeight);
                g2d.drawLine(baseX + extensionWidth, baseY + mainHeight, 
                             baseX + mainWidth, baseY + mainHeight);
                
                drawLShapeGrid(g2d, baseX, baseY, widthPixels, heightPixels);
                break;
                
            case "Square":
                int size = Math.min(widthPixels, heightPixels);
                g2d.fillRect(baseX, baseY, size, size);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(baseX, baseY, size, size);
                drawGrid(g2d, baseX, baseY, size, size);
                break;
        }

        for (FurnitureItem item : furnitureItems) {
            item.draw(g2d, item == selectedItem);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawString(String.format("%.1fm x %.1fm (%s)", roomWidth, roomHeight, roomShape),
                60, 70);

        g2d.drawString("Right-click furniture for options", 60, getHeight() - 30);
        g2d.drawString("Use +/- to resize, R to rotate", 60, getHeight() - 15);
    }

    private void drawGrid(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(200, 200, 200, 100));
        int gridSize = 10;
        
        for (int i = x; i <= x + width; i += gridSize) {
            g2d.drawLine(i, y, i, y + height);
        }
        
        for (int j = y; j <= y + height; j += gridSize) {
            g2d.drawLine(x, j, x + width, j);
        }
    }

    private void drawLShapeGrid(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(200, 200, 200, 100));
        int gridSize = 10;
        
        int mainHeight = height/2;
        for (int i = x; i <= x + width; i += gridSize) {
            g2d.drawLine(i, y, i, y + mainHeight);
        }
        for (int j = y; j <= y + mainHeight; j += gridSize) {
            g2d.drawLine(x, j, x + width, j);
        }
        
        int extensionWidth = width/2;
        for (int i = x; i <= x + extensionWidth; i += gridSize) {
            g2d.drawLine(i, y + mainHeight, i, y + height);
        }
        for (int j = y + mainHeight; j <= y + height; j += gridSize) {
            g2d.drawLine(x, j, x + extensionWidth, j);
        }
    }

    private int getRoomWidthPixels() {
        return (int) (roomWidth * 50);
    }

    private int getRoomHeightPixels() {
        return (int) (roomHeight * 50);
    }

    @Override
    public void onRoomConfigChanged(double width, double height, String shape, Color color) {
        this.roomWidth = width;
        this.roomHeight = height;
        this.roomShape = shape;
        this.roomColor = color;
        repaint();
    }

    public DesignData getDesignData() {
        return new DesignData(roomWidth, roomHeight, roomShape, roomColor,
                new ArrayList<>(furnitureItems));
    }

    public void setDesignData(DesignData data) {
        this.roomWidth = data.roomWidth;
        this.roomHeight = data.roomHeight;
        this.roomShape = data.roomShape;
        this.roomColor = data.roomColor;
        this.furnitureItems = new ArrayList<>(data.furnitureItems);
        
        // Reload images for all furniture items after loading
        for (FurnitureItem item : furnitureItems) {
            item.loadImage();
        }
        
        repaint();
    }
}

interface SelectionListener {
    void onSelectionChanged(FurnitureItem selectedItem);
}

class FurnitureItem implements Serializable {
    private String type;
    private int x, y, width, height;
    private int originalWidth, originalHeight;
    private int rotation = 0;
    private transient Image image;
    private static final Map<String, Image> imageCache = new HashMap<>();

    public FurnitureItem(String type, int x, int y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.originalWidth = width;
        this.originalHeight = height;
        loadImage();
    }

    public void loadImage() {
        if (imageCache.containsKey(type)) {
            image = imageCache.get(type);
        } else {
            try {
                String imagePath = "/" + type.toLowerCase() + ".png";
                ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    imageCache.put(type, image);
                } else {
                    image = null;
                }
            } catch (Exception e) {
                image = null;
            }
        }
    }

    public void draw(Graphics2D g2d, boolean selected) {
        AffineTransform oldTransform = g2d.getTransform();
        
        g2d.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, y, width, height, 10, 10);
            
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(type);
            g2d.drawString(type, x + (width - textWidth) / 2, y + height / 2 + fm.getAscent() / 2 - 2);
        }

        if (selected) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x - 1, y - 1, width + 2, height + 2, 10, 10);
        }
        
        g2d.setTransform(oldTransform);
    }

    public boolean contains(Point p) {
        Rectangle rect = new Rectangle(x, y, width, height);
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        Shape rotatedRect = transform.createTransformedShape(rect);
        return rotatedRect.contains(p);
    }

    public void rotate(int degrees) {
        rotation = (rotation + degrees) % 360;
        if (degrees % 180 != 0) {
            int temp = width;
            width = height;
            height = temp;
        }
    }

    public void resize(double scaleFactor) {
        int newWidth = (int)(width * scaleFactor);
        int newHeight = (int)(height * scaleFactor);
        
        if (newWidth < 20) newWidth = 20;
        if (newHeight < 20) newHeight = 20;
        
        if (newWidth > 500) newWidth = 500;
        if (newHeight > 500) newHeight = 500;
        
        int centerX = x + width/2;
        int centerY = y + height/2;
        
        this.width = newWidth;
        this.height = newHeight;
        
        this.x = centerX - width/2;
        this.y = centerY - height/2;
        
        if (image != null) {
            ImageIcon icon = new ImageIcon(image);
            image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }
}

class DesignData implements Serializable {
    double roomWidth;
    double roomHeight;
    String roomShape;
    Color roomColor;
    ArrayList<FurnitureItem> furnitureItems;

    public DesignData(double width, double height, String shape, Color color,
            ArrayList<FurnitureItem> items) {
        this.roomWidth = width;
        this.roomHeight = height;
        this.roomShape = shape;
        this.roomColor = color;
        this.furnitureItems = items;
    }
}