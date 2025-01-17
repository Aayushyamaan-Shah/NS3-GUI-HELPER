package FileHandler;

import Dialogs.Dialog_ConfigureClient;
import Dialogs.Dialog_ConfigureServer;
import Dialogs.Dialog_Connection;
import Dialogs.Dialog_Link;
import Devices.Device;
import Links.NetworkLink;

import java.util.Map;

public class CodeGenerator {

    // map fields can be declared here....
    public static final String UTILITY_NETANIM = "netanim";
    public static final String UTILITY_WIRESHARK = "wireshark";
    public static final String NAME_OF_TOPOLOGY = "custom";
    public static final String TOTAL_NODES = "nodes";
    public static final String VALUE_TRUE = "t";
    public static final String VALUE_FALSE = "f";

    private Dialog_ConfigureServer dialogConfigureServer;
    private Dialog_ConfigureClient dialogConfigureClient;
    private Dialog_Connection dialogConnection;
    private Dialog_Link dialogLink;
    private Map<String, String> otherFields;
    protected String code;

    public CodeGenerator(Dialog_ConfigureServer s, Dialog_ConfigureClient c, Dialog_Connection conn, Dialog_Link link, Map<String, String> other) {
        this.dialogConfigureServer = s;
        this.dialogConfigureClient = c;
        this.dialogConnection = conn;
        this.dialogLink = link;
        this.otherFields = other;
    }

    public void GenerateCode() {
        // some variable parameters configuration ======================================================================
        String topology = this.otherFields.get(NAME_OF_TOPOLOGY);

        String netAnimModuleString = """
                #include "ns3/netanim-module.h"
                """;
        String netanimUtilityString = """
                AnimationInterface anim(\"animation"""+topology+"""
                .xml\"); 
                """;
        String wiresharkUtilityString = """
                pointToPoint.EnablePcapAll("code");
                """;
        int StopTimeServer = this.dialogConfigureServer.getStopTime();
        int StopTimeClient = this.dialogConfigureClient.getStopTime();
        if (this.otherFields.get(UTILITY_WIRESHARK).compareToIgnoreCase(VALUE_FALSE) == 0) {
            wiresharkUtilityString = "";
        }
        if (this.otherFields.get(UTILITY_NETANIM).compareToIgnoreCase(VALUE_FALSE) == 0) {
            netAnimModuleString = "";
            netanimUtilityString = "";
        }
        String nodesGrp = new String();
        String nodesGrpCode = new String();
        String linkConfigCode = new String();
        String devicesGrp = new String();
        String deviceConfigCode = new String();
        String ipConfigCode = new String();
        String primaryServerGrp = new String();
        String serverPrimaryIndex = new String();
        boolean serverPrimaryConfigured = false;

        for(Device device : this.dialogConnection.devices) {
            nodesGrp = nodesGrp.concat(device.getNodesGroup()+",");
            if (!serverPrimaryConfigured) {
                if (device.nodeA.compareToIgnoreCase(this.dialogConfigureServer.getServerIndex())==0) {
                    primaryServerGrp = device.nodesGroup;
                    serverPrimaryIndex = "0";
                    serverPrimaryConfigured = true;
                } else if (device.nodeB.compareToIgnoreCase(this.dialogConfigureServer.getServerIndex()) == 0) {
                    primaryServerGrp = device.nodesGroup;
                    serverPrimaryIndex = "1";
                    serverPrimaryConfigured = true;
                }
                // System.out.println("Primary Index : "+serverPrimaryIndex+" Primary Group : "+primaryServerGrp);
            }
        }
        nodesGrp = nodesGrp.substring(0,nodesGrp.length()-1);

        for (Device device : this.dialogConnection.devices) {
            nodesGrpCode = nodesGrpCode.concat(device.getNodesGroupCode()+"\n");
        }

        for (NetworkLink link : this.dialogLink.getAllLinks()) {
            linkConfigCode = linkConfigCode.concat(link.toCode()+"\n");
        }

        for (Device device : this.dialogConnection.devices) {
            devicesGrp = devicesGrp.concat(device.getDevicesGroup()+",");
        }
        devicesGrp = devicesGrp.substring(0,devicesGrp.length()-1);

        for (Device device : this.dialogConnection.devices) {
            deviceConfigCode = deviceConfigCode.concat(device.getDeviceConfCode());
        }

        for (Device device : this.dialogConnection.devices) {
            ipConfigCode = ipConfigCode.concat(device.getIPConfCode()+"\n");
        }
        // variable parameters configuration ends ======================================================================

        this.code = """
                #include "ns3/applications-module.h"
                #include "ns3/core-module.h"
                #include "ns3/internet-module.h"
                #include "ns3/network-module.h"
                #include "ns3/point-to-point-module.h"
                """
                + netAnimModuleString +
                """
                                
                using namespace ns3;
                                
                NS_LOG_COMPONENT_DEFINE(\""""+topology+"""
                Example\");
                                
                int
                main(int argc, char* argv[])
                {
                    CommandLine cmd(__FILE__);
                    cmd.Parse(argc, argv);
                                
                    Time::SetResolution(Time::NS);
                    LogComponentEnable("UdpEchoClientApplication", LOG_LEVEL_INFO);
                    LogComponentEnable("UdpEchoServerApplication", LOG_LEVEL_INFO);
                   \s
                    // step-1 = creating group of nodes....
                    NodeContainer allNodes,"""
                + nodesGrp +
                """
                ;
                allNodes.Create("""
                + this.otherFields.get(TOTAL_NODES) +
                """
                );
                
                """
                + nodesGrpCode +
                """
                            
                // step-2 = create link
                """
                + linkConfigCode +
                """
               \s
                // step-3 = creating devices
                NetDeviceContainer   """
                + devicesGrp +
                """
                ;
                
                """
                + deviceConfigCode +
                """
                            
                // step-4 = Install ip stack
                InternetStackHelper stack;
                stack.Install(allNodes);
               
                // step-5 = Assignment of IP Address
                Ipv4AddressHelper address;
               
                """
                + ipConfigCode +
                """
              
                // step-6 = server configuration
                UdpEchoServerHelper echoServer("""
                + this.dialogConfigureServer.getPortNumber() +"""
                    );
                                
                    ApplicationContainer serverApps = echoServer.Install(allNodes.Get("""
                + this.dialogConfigureServer.getServerIndex() + """
                    ));
                    serverApps.Start(Seconds("""+ this.dialogConfigureServer.getStartTime()  +"""
                    .0));
                    serverApps.Stop(Seconds("""+ StopTimeServer +"""
                    .0));
                   
                    // step-7 = client configuration
                    UdpEchoClientHelper echoClient(interfaces"""+ primaryServerGrp +"""
                    .GetAddress("""+ serverPrimaryIndex + """
                ),"""+ this.dialogConfigureServer.getPortNumber() +"""
                    );
                    echoClient.SetAttribute("MaxPackets", UintegerValue("""+ this.dialogConfigureClient.getPackets() +"""
                    ));
                    echoClient.SetAttribute("Interval", TimeValue(Seconds("""+ this.dialogConfigureClient.getInterval() +"""
                    .0)));
                    echoClient.SetAttribute("PacketSize", UintegerValue("""+ this.dialogConfigureClient.getMTU() +"""
                    ));
                                
                    ApplicationContainer clientApps = echoClient.Install(allNodes.Get("""+ this.dialogConfigureClient.getClientIndex() +"""
                    ));
                    clientApps.Start(Seconds("""+ this.dialogConfigureClient.getStartTime() +"""
                    .0));
                    clientApps.Stop(Seconds("""+ StopTimeClient +"""
                    .0));
                   
                    Ipv4GlobalRoutingHelper::PopulateRoutingTables();
                    """
                + netanimUtilityString + "\n" + wiresharkUtilityString +
                """
               
                            
                Simulator::Run();
                Simulator::Destroy();
                return 0;
            }
            """;
    }

    public String getCode() {
        return this.code;
    }

}
