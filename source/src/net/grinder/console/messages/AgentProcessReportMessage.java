// Copyright (C) 2005 Philip Aston
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

package net.grinder.console.messages;

import net.grinder.common.AgentIdentity;
import net.grinder.common.AgentProcessReport;
import net.grinder.communication.Message;


/**
 * Message for informing the console of agent process status.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public final class AgentProcessReportMessage
  implements Message, AgentProcessReport {

  private static final long serialVersionUID = -2073574340466531680L;

  private final AgentIdentity m_identity;
  private final short m_state;

  /**
   * Creates a new <code>AgentProcessReportMessage</code> instance.
   *
   * @param identity Process identity.
   * @param state The process state. See {@link
   * net.grinder.common.AgentProcessReport}.
   */
  public AgentProcessReportMessage(AgentIdentity identity, short state) {
    m_identity = identity;
    m_state = state;
  }

  /**
   * Accessor for the process identity.
   *
   * @return The process identity.
   */
  public ProcessIdentity getIdentity() {
    return m_identity;
  }

  /**
   * Accessor for the process identity.
   *
   * @return The process identity.
   */
  public AgentIdentity getAgentIdentity() {
    return m_identity;
  }

  /**
   * Accessor for the process state.
   *
   * @return The process state.
   */
  public short getState() {
    return m_state;
  }
}
