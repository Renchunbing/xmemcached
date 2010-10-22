/**
 *Copyright [2009-2010] [dennis zhuang(killme2008@gmail.com)]
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *             http://www.apache.org/licenses/LICENSE-2.0
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *either express or implied. See the License for the specific language governing permissions and limitations under the License
 */
package net.rubyeye.xmemcached.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import net.rubyeye.xmemcached.CommandFactory;
import net.rubyeye.xmemcached.MemcachedOptimizer;
import net.rubyeye.xmemcached.buffer.BufferAllocator;
import net.rubyeye.xmemcached.command.Command;
import net.rubyeye.xmemcached.command.OperationStatus;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.networking.MemcachedSession;

import com.google.code.yanf4j.core.WriteMessage;
import com.google.code.yanf4j.core.impl.FutureImpl;
import com.google.code.yanf4j.nio.NioSessionConfig;
import com.google.code.yanf4j.nio.impl.NioTCPSession;
import com.google.code.yanf4j.util.LinkedTransferQueue;
import com.google.code.yanf4j.util.SystemUtils;

/**
 * Connected session for a memcached server
 * 
 * @author dennis
 */
public class MemcachedTCPSession extends NioTCPSession implements
		MemcachedSession {

	/**
	 * Command which are already sent
	 */
	protected BlockingQueue<Command> commandAlreadySent;

	private volatile int weight;

	private int order;

	private final AtomicReference<Command> currentCommand = new AtomicReference<Command>();

	private SocketAddress remoteSocketAddress; // prevent channel is closed
	private int sendBufferSize;
	private final MemcachedOptimizer optimiezer;
	private volatile boolean allowReconnect;

	private volatile boolean authFailed;

	private final CommandFactory commandFactory;

	public final int getWeight() {
		return weight;
	}

	public final void setWeight(int weight) {
		this.weight = weight;
	}

	public MemcachedTCPSession(NioSessionConfig sessionConfig,
			int readRecvBufferSize, MemcachedOptimizer optimiezer,
			int readThreadCount, CommandFactory commandFactory) {
		super(sessionConfig, readRecvBufferSize);
		this.optimiezer = optimiezer;
		if (selectableChannel != null) {
			remoteSocketAddress = ((SocketChannel) selectableChannel).socket()
					.getRemoteSocketAddress();
			allowReconnect = true;
			try {
				sendBufferSize = ((SocketChannel) selectableChannel).socket()
						.getSendBufferSize();
			} catch (SocketException e) {
				sendBufferSize = 8 * 1024;
			}
		}
		commandAlreadySent = new LinkedTransferQueue<Command>();
		this.commandFactory = commandFactory;
	}

	public final int getOrder() {
		return order;
	}

	public final void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return SystemUtils.getRawAddress(getRemoteSocketAddress()) + ":"
				+ getRemoteSocketAddress().getPort();
	}

	public void destroy() {
		Command command = currentCommand.get();
		if (command != null) {
			command.setException(new MemcachedException(
					"Session has been closed"));
			command.getLatch().countDown();
		}
		while ((command = commandAlreadySent.poll()) != null) {
			command.setException(new MemcachedException(
					"Session has been closed"));
			if (command.getLatch() != null) {
				command.getLatch().countDown();
			}
		}

	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		InetSocketAddress result = super.getRemoteSocketAddress();
		if (result == null && remoteSocketAddress != null) {
			result = (InetSocketAddress) remoteSocketAddress;
		}
		return result;
	}

	@Override
	protected WriteMessage preprocessWriteMessage(WriteMessage writeMessage) {
		Command currentCommand = (Command) writeMessage;
		// Check if IoBuffer is null
		if (currentCommand.getIoBuffer() == null) {
			currentCommand.encode();
		}
		if (currentCommand.getStatus() == OperationStatus.SENDING) {
			/**
			 * optimieze commands
			 */
			currentCommand = optimiezer.optimize(currentCommand, writeQueue,
					commandAlreadySent, sendBufferSize);
		}
		currentCommand.setStatus(OperationStatus.WRITING);
		return currentCommand;
	}

	public boolean isAuthFailed() {
		return authFailed;
	}

	public void setAuthFailed(boolean authFailed) {
		this.authFailed = authFailed;
	}

	private BufferAllocator bufferAllocator;

	public final BufferAllocator getBufferAllocator() {
		return bufferAllocator;
	}

	public final void setBufferAllocator(BufferAllocator bufferAllocator) {
		this.bufferAllocator = bufferAllocator;
	}

	@Override
	protected final WriteMessage wrapMessage(Object msg,
			Future<Boolean> writeFuture) {
		((Command) msg).encode();
		((Command) msg).setWriteFuture((FutureImpl<Boolean>) writeFuture);
		if (log.isDebugEnabled()) {
			log.debug("After encoding" + ((Command) msg).toString());
		}
		return (WriteMessage) msg;
	}

	/**
	 * get current command from queue
	 * 
	 * @return
	 */
	private final Command takeExecutingCommand() {
		try {
			return commandAlreadySent.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	/**
	 * is allow auto recconect if closed?
	 * 
	 * @return
	 */
	public boolean isAllowReconnect() {
		return allowReconnect;
	}

	public void setAllowReconnect(boolean reconnected) {
		allowReconnect = reconnected;
	}

	public final void addCommand(Command command) {
		commandAlreadySent.add(command);
	}

	public final void setCurrentCommand(Command cmd) {
		currentCommand.set(cmd);
	}

	public final Command getCurrentCommand() {
		return currentCommand.get();
	}

	public final void takeCurrentCommand() {
		setCurrentCommand(takeExecutingCommand());
	}

	public void quit() {
		write(commandFactory.createQuitCommand());
	}
}
