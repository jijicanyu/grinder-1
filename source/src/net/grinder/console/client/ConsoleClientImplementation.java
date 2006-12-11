// Copyright (C) 2006 Philip Aston
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
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.client;

import net.grinder.communication.BlockingSender;
import net.grinder.communication.CommunicationException;
import net.grinder.console.communication.server.messages.ResetRecordingMessage;
import net.grinder.console.communication.server.messages.StartRecordingMessage;
import net.grinder.console.communication.server.messages.StopRecordingMessage;



/**
 * Implementation of {@link ConsoleClient} that uses a {@link BlockingSender} to
 * communicate with the console.
 *
 * @author Philip Aston
 * @version $Revision:$
 */
public final class ConsoleClientImplementation implements ConsoleClient {

  private final BlockingSender m_consoleSender;

  ConsoleClientImplementation(BlockingSender consoleSender) {
    m_consoleSender = consoleSender;
  }

  /**
   * Start the console recording.
   *
   * @throws ConsoleClientException If a communication error occurred.
   */
  public void startRecording() throws ConsoleClientException {
    try {
      m_consoleSender.blockingSend(new StartRecordingMessage());
    }
    catch (CommunicationException e) {
      throw new ConsoleClientException("Failed to start recording", e);
    }
  }

  /**
   * Stop the console recording.
   *
   * @throws ConsoleClientException If a communication error occurred.
   */
  public void stopRecording() throws ConsoleClientException {
    try {
      m_consoleSender.blockingSend(new StopRecordingMessage());
    }
    catch (CommunicationException e) {
      throw new ConsoleClientException("Failed to stop recording", e);
    }
  }

  /**
   * Reset the console recording.
   *
   * @throws ConsoleClientException If a communication error occurred.
   */
  public void resetRecording() throws ConsoleClientException {
    try {
      m_consoleSender.blockingSend(new ResetRecordingMessage());
    }
    catch (CommunicationException e) {
      throw new ConsoleClientException("Failed to reset recording", e);
    }
  }
}