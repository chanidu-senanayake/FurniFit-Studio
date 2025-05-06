package com.mycompany.furnituredesignapp;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.vecmath.*;

public class FurnitureDesignApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

interface SelectionListener {
    void onSelectionChanged(FurnitureItem3D item);
}

interface RoomConfigListener {
    void onRoomConfigChanged(double width, double height, String shape, Color color);
}

class DesignData implements Serializable {
    public double roomWidth;
    public double roomHeight;
    public String roomShape;
    public Color roomColor;
    public ArrayList<FurnitureItem3D> furnitureItems;

    public DesignData(double width, double height, String shape, Color color, 
                     ArrayList<FurnitureItem3D> items) {
        this.roomWidth = width;
        this.roomHeight = height;
        this.roomShape = shape;
        this.roomColor = color;
        this.furnitureItems = new ArrayList<>(items);
    }
}

class RoomConfigPanel extends JPanel {
    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> shapeCombo;
    private JButton colorButton;
    private Color currentColor = Color.LIGHT_GRAY;
    private RoomConfigListener listener;

    public RoomConfigPanel() {
        setLayout(new GridLayout(4, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Room Configuration"));

        add(new JLabel("Width (m):"));
        widthField = new JTextField("5.0");
        add(widthField);

        add(new JLabel("Height (m):"));
        heightField = new JTextField("3.0");
        add(heightField);

        add(new JLabel("Shape:"));
        shapeCombo = new JComboBox<>(new String[]{"Rectangle", "Square", "L-Shaped"});
        add(shapeCombo);

        add(new JLabel("Color:"));
        colorButton = new JButton("Choose");
        colorButton.addActionListener(e -> chooseColor());
        add(colorButton);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyConfig());
        add(applyButton);
    }

    public void setConfigListener(RoomConfigListener listener) {
        this.listener = listener;
    }

    private void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Room Color", currentColor);
        if (newColor != null) {
            currentColor = newColor;
            colorButton.setBackground(newColor);
        }
    }

    private void applyConfig() {
        try {
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            String shape = (String) shapeCombo.getSelectedItem();
            
            if (listener != null) {
                listener.onRoomConfigChanged(width, height, shape, currentColor);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setRoomDimensions(double width, double height) {
        widthField.setText(String.valueOf(width));
        heightField.setText(String.valueOf(height));
    }
}

class FurnitureSelectionPanel extends JPanel {
    private DesignPanel3D designPanel;
    private ButtonGroup furnitureGroup;

    public FurnitureSelectionPanel(DesignPanel3D designPanel) {
        this.designPanel = designPanel;
        setLayout(new GridLayout(0, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Furniture Selection"));

        String[] furnitureTypes = {"Chair", "Table", "Sofa", "Bed", "Cabinet", "Lamp"};
        furnitureGroup = new ButtonGroup();

        for (String type : furnitureTypes) {
            JToggleButton button = new JToggleButton(type);
            button.addActionListener(e -> {
                if (button.isSelected()) {
                    designPanel.setSelectedFurniture(type);
                } else {
                    designPanel.setSelectedFurniture(null);
                }
            });
            furnitureGroup.add(button);
            add(button);
        }
    }
}

class MainFrame extends JFrame {
    private DesignPanel3D designPanel;
    private RoomConfigPanel configPanel;
    private FurnitureSelectionPanel furniturePanel;
    private JButton deleteButton, rotateButton, increaseSizeButton, decreaseSizeButton, clearButton;

    public MainFrame() {
        setTitle("3D Furniture Design Application");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu setup
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

        // Panel setup
        configPanel = new RoomConfigPanel();
        designPanel = new DesignPanel3D();
        furniturePanel = new FurnitureSelectionPanel(designPanel);

        // Button setup
        deleteButton = new JButton("Delete Selected (Del)");
        deleteButton.addActionListener(e -> designPanel.deleteSelectedFurniture());
        deleteButton.setEnabled(false);

        rotateButton = new JButton("Rotate (R)");
        rotateButton.addActionListener(e -> designPanel.rotateSelectedFurniture(90));
        rotateButton.setEnabled(false);

        increaseSizeButton = new JButton("Increase Size (+)");
        increaseSizeButton.addActionListener(e -> designPanel.resizeSelectedFurniture(1.1f));
        increaseSizeButton.setEnabled(false);

        decreaseSizeButton = new JButton("Decrease Size (-)");
        decreaseSizeButton.addActionListener(e -> designPanel.resizeSelectedFurniture(0.9f));
        decreaseSizeButton.setEnabled(false);

        clearButton = new JButton("Clear Design");
        clearButton.addActionListener(e -> clearDesign());

        designPanel.addSelectionListener(selected -> {
            deleteButton.setEnabled(selected != null);
            rotateButton.setEnabled(selected != null);
            increaseSizeButton.setEnabled(selected != null);
            decreaseSizeButton.setEnabled(selected != null);
        });

        // Layout setup
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(configPanel, BorderLayout.NORTH);
        leftPanel.add(furniturePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
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

class DesignPanel3D extends GLCanvas implements GLEventListener, RoomConfigListener {
    private static final int FPS = 60;
    private FPSAnimator animator;
    private double roomWidth = 5.0, roomHeight = 3.0, roomDepth = 4.0;
    private String roomShape = "Rectangle";
    private Color3f roomColor = new Color3f(0.8f, 0.8f, 0.8f);
    private String selectedFurnitureType = null;
    private ArrayList<FurnitureItem3D> furnitureItems = new ArrayList<>();
    private FurnitureItem3D selectedItem = null;
    private Point dragStart = null;
    private boolean isDragging = false;
    private float cameraX = 0f, cameraY = 0f, cameraZ = 10f;
    private float lookAtX = 0f, lookAtY = 0f, lookAtZ = 0f;
    private float upX = 0f, upY = 1f, upZ = 0f;
    private ArrayList<SelectionListener> selectionListeners = new ArrayList<>();

    public DesignPanel3D() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        setCapabilities(capabilities);
        addGLEventListener(this);
        animator = new FPSAnimator(this, FPS);
        animator.start();
        setupMouseListeners();
    }

    // Implement all required GLEventListener methods
    @Override public void init(GLAutoDrawable drawable) { /* implementation */ }
    @Override public void display(GLAutoDrawable drawable) { /* implementation */ }
    @Override public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }
    @Override public void dispose(GLAutoDrawable drawable) { animator.stop(); }

    // Other methods...
    public void setSelectedFurniture(String type) { this.selectedFurnitureType = type; }
    public void clearDesign() { furnitureItems.clear(); setSelectedItem(null); }
    // Add all other required methods
}

class FurnitureItem3D implements Serializable {
    private String type;
    private float x, y, z;
    private float width, height, depth;
    private float rotationY = 0;
    private transient Color3f color;
    
    public FurnitureItem3D(String type, float x, float y, float z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        setDefaultDimensions();
        setDefaultColor();
    }
    
    private void setDefaultDimensions() {
        switch (type.toLowerCase()) {
            case "chair": width = 0.5f; height = 0.8f; depth = 0.5f; break;
            case "table": width = 1.2f; height = 0.7f; depth = 1.2f; break;
            case "sofa": width = 1.8f; height = 0.7f; depth = 0.8f; break;
            case "bed": width = 2.0f; height = 0.5f; depth = 1.5f; break;
            case "cabinet": width = 0.8f; height = 1.5f; depth = 0.5f; break;
            case "lamp": width = 0.3f; height = 1.0f; depth = 0.3f; break;
            default: width = 0.5f; height = 0.5f; depth = 0.5f;
        }
    }
    
    private void setDefaultColor() {
        switch (type.toLowerCase()) {
            case "chair": color = new Color3f(0.8f, 0.2f, 0.2f); break;
            case "table": color = new Color3f(0.6f, 0.4f, 0.2f); break;
            case "sofa": color = new Color3f(0.2f, 0.2f, 0.8f); break;
            case "bed": color = new Color3f(0.9f, 0.9f, 0.9f); break;
            case "cabinet": color = new Color3f(0.5f, 0.3f, 0.1f); break;
            case "lamp": color = new Color3f(0.9f, 0.9f, 0.1f); break;
            default: color = new Color3f(0.5f, 0.5f, 0.5f);
        }
    }
    
    public void draw(GL2 gl, boolean selected) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glRotatef(rotationY, 0, 1, 0);
        gl.glColor3f(selected ? 1f : color.x, selected ? 0f : color.y, selected ? 0f : color.z);
        
        switch (type.toLowerCase()) {
            case "chair": drawChair(gl); break;
            case "table": drawTable(gl); break;
            case "sofa": drawSofa(gl); break;
            case "bed": drawBed(gl); break;
            case "cabinet": drawCabinet(gl); break;
            case "lamp": drawLamp(gl); break;
            default: drawCube(gl);
        }
        gl.glPopMatrix();
    }
    
    private void drawCube(GL2 gl) {
        float hw = width/2, hh = height/2, hd = depth/2;
        gl.glBegin(GL2.GL_QUADS);
        // Front, back, top, bottom, left, right faces
        gl.glEnd();
    }
    
    // Implement other drawing methods (drawChair, drawTable, etc.)
}