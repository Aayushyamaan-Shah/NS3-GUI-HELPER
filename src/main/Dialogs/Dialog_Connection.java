package Dialogs;

import Helpers.DebuggingHelper;
import Devices.Device;
import Links.NetworkLink;
import Netowkrs.Network;
import StatusHelper.LinkType;
import StatusHelper.ToolStatus;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Dialog_Connection extends JFrame {
    private JPanel JPanel_main;
    private JLabel lbl_deviceInfo;
    private JPanel JPanel_group;
    private JLabel lbl_link;
    private JComboBox comboBox_links;
    private JLabel lbl_network;
    private JComboBox comboBox_networks;
    private JButton btn_configureConnection;

    // for serving the functionality....
    public int nodeA, nodeB;
    public ArrayList<Integer> nodes;
    public ArrayList<NetworkLink> links;
    public ArrayList<Network> networks;
    public ArrayList<Device> devices;

    Image img;
    {
        try {
            img = ImageIO.read(getClass().getClassLoader().getResource("info.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Dialog_Connection() {
        this(0,0,new ArrayList<NetworkLink>(), new ArrayList<Network>());
    }

    public Dialog_Connection(int a, int b, ArrayList<NetworkLink> l, ArrayList<Network> n) {
        // initializing this component....
        this.setContentPane(this.JPanel_main);
        this.setTitle("Configure Connection");
        this.setSize(400,200);
        this.setVisible(false);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        this.nodeA = a;
        this.nodeB = b;
        this.links = new ArrayList<>();
        this.links.addAll(l);
        this.networks = new ArrayList<>();
        this.networks.addAll(n);
        this.devices = new ArrayList<>();

        this.lbl_deviceInfo.setText("Connection between node "+this.nodeA+" and node "+this.nodeB);
        Image scaledImg = img.getScaledInstance(30,30,Image.SCALE_SMOOTH);
        ImageIcon infoIcon = new ImageIcon(scaledImg);
        this.lbl_deviceInfo.setIcon(infoIcon);

        // action to perform when clicking on configure connection button...
        btn_configureConnection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                devices.add(
                    new Device
                    (
                        links.get
                        (
                            comboBox_links.getSelectedIndex()
                        ),
                        networks.get(comboBox_networks.getSelectedIndex()), 
                        String.valueOf(nodeA), String.valueOf(nodeB)
                    )
                );
                for (Device d : devices) {
                    DebuggingHelper.Debugln(d.toString());
                }
                setVisible(false);
            }
        });
    }

    public void setVisible(boolean b, ToolStatus selectedTool) {
        super.setVisible(b);
        if (b) {
            LinkType type;
            if (selectedTool == ToolStatus.TOOL_LINK) {
                this.lbl_deviceInfo.setText("Connection between node "+this.nodeA+" and node "+this.nodeB);
                type = LinkType.LINK_P2P;
            } else { // assumed to be CSMA...
                type = LinkType.LINK_CSMA;
                String list_nodes = this.nodes.stream().map(Object::toString).collect(Collectors.joining(", "));
                this.lbl_deviceInfo.setText("CSMA Connection between nodes : "+list_nodes);
            }
            this.comboBox_links.removeAllItems();
            this.comboBox_networks.removeAllItems();
            for (NetworkLink link : this.links) {
                if (type == link.getLinkType()) {
                    this.comboBox_links.addItem(link);
                }
            }
            for (Network n : this.networks) {
                this.comboBox_networks.addItem(n);
            }
        }
    }

    public void showDialog(ArrayList<NetworkLink> l, ArrayList<Network> n,ArrayList<Integer> nodes, ToolStatus selectedTool) {
        if (selectedTool == ToolStatus.TOOL_LINK) {
            this.nodeA = nodes.get(0);
            this.nodeB = nodes.get(1);
        } else {
            this.nodes = nodes;
        }
        this.setLinks(l);
        this.setNetworks(n);
        this.setVisible(true, selectedTool);
    }

    public void setLinks(ArrayList<NetworkLink> links) {
        this.links = links;
    }

    public void setNetworks(ArrayList<Network> networks) {
        this.networks = networks;
    }
}
