package components;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import interfaces.PositionI;
import interfaces.RegistrationCI;
import ports.SimulatorInboundPort;
import interfaces.P2PAddressI;

@OfferedInterfaces(offered = {RegistrationCI.class})
public class SimulatorComponent extends AbstractComponent 
{
	protected SimulatorInboundPort inboundPort;
	public static final String REGISTRATION_NODE_INBOUND_PORT_URI = "s-in-uri";
	private Set<SimulatorConnectionInfo> node = new HashSet<>();

	protected SimulatorComponent() throws Exception {
		super(1, 0);
		this.inboundPort = new SimulatorInboundPort(REGISTRATION_NODE_INBOUND_PORT_URI, this);
		this.inboundPort.publishPort();
	}
	
	public synchronized Set<ConnectionInfo> registerTerminalNode(P2PAddressI address, String communicationInboundPort, PositionI initialPosition, double initialRange, String routingInboundPortURI) throws Exception
	{
		SimulatorConnectionInfo ci = new SimulatorConnectionInfo(
			address,
			communicationInboundPort, 
			routingInboundPortURI, 
			initialPosition,
			initialRange,
			NodeType.INTERNAL
		);
		return this.getInRange(ci);
	}

	public synchronized Set<ConnectionInfo> registerAccessPoint(P2PAddressI address, String communicationInboundPort, PositionI initialPosition, double initialRange, String routingInboundPortURI) throws Exception
	{
		SimulatorConnectionInfo ci = new SimulatorConnectionInfo(
			address, 
			communicationInboundPort, 
			routingInboundPortURI, 
			initialPosition,
			initialRange,
			NodeType.ACCESSPOINT
		);		
		return this.getInRange(ci);
	}
	
	private synchronized Set<ConnectionInfo> getInRange(SimulatorConnectionInfo ci) throws Exception {
		Set<ConnectionInfo> res = new HashSet<ConnectionInfo>();
		for(SimulatorConnectionInfo c : this.node)
		{
			if(ci.getInitialPosition().distance(c.getInitialPosition()) <= ci.getInitialRange()) {
				res.add(c);
			}
		}
		node.add(ci);
		return res;
	}
	
	public synchronized void unregister(P2PAddressI address) throws Exception
	{
		for(ConnectionInfo c : this.node)
		{
			if(c.getAddress().equals(address)) {
				node.remove(c);
				break;
			}
		}
	}	
	
	@Override
	public synchronized void shutdown() throws ComponentShutdownException
	{
		try {
			this.inboundPort.unpublishPort();
		}  catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
		super.shutdown();
	}
}
