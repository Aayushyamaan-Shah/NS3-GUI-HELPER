package GuiHelpers;


import java.awt.*;
import java.util.ArrayList;

public class TopologyPainter extends Canvas {
    private static int DEFAULT_WIDTH = 500;
    private static int DEFAULT_HEIGHT = 500;
    private static int SERVER_NODE = -1;
    private static int CLIENT_NODE = -1;
    private static NodeRenderer HIGHLIGHT = new NodeRenderer(0,0, 30, "SERVER", Color.black);
    // below is the grid configuration...
    public int[][] GRID_X0 = new int[][] {{0,0},{100,0},{100,100},{0,100}};
    public int[][] GRID_X1 = new int[][] {{200,0},{300,0},{300,100},{200,100}};
    public int[][] GRID_X2 = new int[][] {{200,200},{300,200},{300,300},{200,300}};
    public int[][] GRID_X3 = new int[][] {{0,200},{100,200},{100,300},{0,300}};
    public int[][][] GRID_MAIN = new int[][][] {GRID_X0, GRID_X1, GRID_X2, GRID_X3};

    // index0 of SUB_GRIDS_INDEX = X0 and so on....
    // public static int GRID_X0_INDEX = 0, GRID_X1_INDEX = 0, GRID_X2_INDEX = 0, GRID_X3_INDEX = 0;
    public int[] SUB_GRIDS_INDEX = new int[] {3,0,1,2};
    public int SUB_GRID_INDEX = 0; // it will range from 0 to GRID_SIZE
    public int GRID_SIZE = 4, GRID_INDEX = 0; // GRID_INDEX will range from 0 to GRID_SIZE

    int width, height;
    ArrayList<NodeRenderer> nodes;
    ArrayList<P2PLinkRederer> links;

    public TopologyPainter(ArrayList<NodeRenderer> n, ArrayList<P2PLinkRederer> l) {
        this(n,l,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    public TopologyPainter(ArrayList<NodeRenderer> n, ArrayList<P2PLinkRederer> l, int w, int h) {
        // initializing components...
        this.nodes = new ArrayList<>();
        this.nodes.addAll(n);
        this.links = new ArrayList<>();
        this.links.addAll(l);
        this.width = w;
        this.height = h;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.clearRect(0,0,this.width,this.height);

        for (int i=0; i<this.nodes.size(); i++) {
            if (SERVER_NODE == i) {
                HIGHLIGHT.label = "SERVER";
                HIGHLIGHT.color = Color.black;
                HIGHLIGHT.xPos = this.nodes.get(i).xPos - 5;
                HIGHLIGHT.yPos = this.nodes.get(i).yPos - 5;
                HIGHLIGHT.paintNode(g);
            }

            if (CLIENT_NODE == i) {
                HIGHLIGHT.label = "CLIENT";
                HIGHLIGHT.color = Color.black;
                HIGHLIGHT.xPos = this.nodes.get(i).xPos - 5;
                HIGHLIGHT.yPos = this.nodes.get(i).yPos - 5;
                HIGHLIGHT.paintNode(g);
            }
            this.nodes.get(i).paintNode(g);
        }

        for (int i=0; i<this.links.size(); i++) {
            this.links.get(i).paintLink(g);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500,500);
    }

    public int[] nextRandomPoint() {
        int[] point = new int[2];

        // generate random...
        // point[0] = xPos, x point will be generated...
        point[0] = getRandom(GRID_MAIN[GRID_INDEX][SUB_GRIDS_INDEX[SUB_GRID_INDEX]][0]);
        // point[1] = yPos, y point will be generated...
        point[1] = getRandom(GRID_MAIN[GRID_INDEX][SUB_GRIDS_INDEX[SUB_GRID_INDEX]][1]);

        // now change the grid where next point will be generated...
        GRID_INDEX = (GRID_INDEX + 1)%GRID_SIZE; // changing the next 200x200 grid... [x0 -> x1 -> x2 -> x3 -> x0 -> ...]
        SUB_GRIDS_INDEX[SUB_GRID_INDEX] = (SUB_GRIDS_INDEX[SUB_GRID_INDEX]+2)%GRID_SIZE; // changing the next 100x100 grid [A0 -> A1 -> A2 -> A3 -> A0 -> ...](if current 100x100 grid is A)
        SUB_GRID_INDEX = (SUB_GRID_INDEX+1)%GRID_SIZE; // changing to the next 100x100 grid...in next 200x200 grid [A -> B -> C -> D -> A -> ...]

        return point;
    }

    public void addNode(NodeRenderer node) {
        this.nodes.add(node);
    }

    public void addLink(P2PLinkRederer link) {
        this.links.add(link);
    }

    public void addAndPrintNode(NodeRenderer node) {
        this.addNode(node);
        this.repaint();
    }

    public void addAndPrintLink(P2PLinkRederer link) {
        this.addLink(link);
        this.repaint();
    }

    public int pointCollideWithAny(int x, int y) {
        int index = -1;

        for (int i=0; i<this.nodes.size(); i++) {
            if (this.checkCollisionWithNode(this.nodes.get(i), x, y)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public void enableView() {
        this.enableView(-1,-1);
    }

    public void enableView(int s, int c) {
        SERVER_NODE = s;
        CLIENT_NODE = c;
        System.out.println("Server : "+s+" client : "+c);
        this.repaint();
    }

    private boolean checkCollisionWithNode(NodeRenderer n, int x, int y) {
        int distance = (int) Math.sqrt(Math.pow(n.xPos - x,2) + Math.pow(n.yPos - y, 2));
        // System.out.println("Distance : "+distance); // just for testing...
        if (distance < n.radius) {
            return true;
        }
        return false;
    }

    private int getRandom(int low) {
        return getRandom(low, low + 100); // 100 = Size of Sub Grids in 2D Space...
    }

    private int getRandom(int low, int high) {
        return (int) (Math.random() * (high - low)) + low;
    }

    public ArrayList<NodeRenderer> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<NodeRenderer> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<P2PLinkRederer> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<P2PLinkRederer> links) {
        this.links = links;
    }
}
