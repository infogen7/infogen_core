/**
 * 
 */
package com.infogen.attach;

import java.io.IOException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * 
 * @author larry
 * @version 创建时间 2019年7月9日 上午5:57:28
 */
public class Attach {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.exit(-1);
		}

		String agent_path = args[0];
		String pid = args[1];
		VirtualMachine virtualmachine_instance = null;
		try {
			virtualmachine_instance = VirtualMachine.attach(pid);
			virtualmachine_instance.loadAgent(agent_path, "");
		} catch (AgentLoadException | AgentInitializationException | IOException | AttachNotSupportedException e) {
			System.exit(-1);
		} finally {
			virtualmachine_instance.detach();
		}
	}
}
