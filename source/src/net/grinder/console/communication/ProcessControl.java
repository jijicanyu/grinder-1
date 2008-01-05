// Copyright (C) 2004, 2005, 2006, 2007 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.communication;

import net.grinder.common.GrinderProperties;


/**
 * Interface for issuing commands to the agent and worker processes.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public interface ProcessControl {

  /**
   * Signal the worker processes to start.
   *
   * @param properties
   *            Properties that override the agent's local properties.
   */
  void startWorkerProcesses(GrinderProperties properties);

  /**
   * Signal the worker processes to reset.
   */
  void resetWorkerProcesses();

  /**
   * Signal the agent and worker processes to stop.
   */
  void stopAgentAndWorkerProcesses();

  /**
   * Add a listener for process status data.
   *
   * @param listener The listener.
   */
  void addProcessStatusListener(ProcessStatus.Listener listener);

  /**
   * How many agents are live?
   *
   * @return The number of agents.
   */
  int getNumberOfLiveAgents();
}
