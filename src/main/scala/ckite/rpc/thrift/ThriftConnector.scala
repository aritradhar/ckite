package ckite.rpc.thrift

import scala.util.Try
import ckite.Member
import ckite.rpc._
import ckite.util.Logging
import com.twitter.finagle.Thrift
import com.twitter.util.Future
import scala.util.Success
import java.nio.ByteBuffer
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import scala.util.Failure
import ckite.rpc.thrift.ThriftConverters._
import scala.collection.concurrent.TrieMap
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.conversions.time._
import com.twitter.finagle.service.RetryPolicy
import com.twitter.util.Throw
import ckite.rlog.Snapshot
import ckite.RemoteMember


class ThriftConnector(binding: String) extends Connector with Logging {

  val client = new CKiteService.FinagledClient(ClientBuilder().hosts(binding)
  				.retryPolicy(NoRetry).codec(ThriftClientFramedCodec()).failFast(false)
  				.hostConnectionLimit(10).hostConnectionCoresize(1).requestTimeout(Duration(60, TimeUnit.SECONDS)).build())
  
  override def send(request: RequestVote): Try[RequestVoteResponse] = {
    Try {
      LOG.debug(s"Sending $request to $binding")
      client.sendRequestVote(request).get
    } 
  }
  
  override def send(appendEntries: AppendEntries): Try[AppendEntriesResponse] = {
   Try {
      LOG.trace(s"Sending $appendEntries to $binding")
      client.sendAppendEntries(appendEntries).get
    }
  }
  
  override def send[T](command: Command): T = {
    val future = client.forwardCommand(command)
    val value: T = future.get
    value
  }
  
  override def send(snapshot: Snapshot) = {
    val future: Future[Boolean] = client.installSnapshot(snapshot)
    future
  }
  
  override def send(joinRequest: JoinRequest): Try[JoinResponse] = {
    Try {
       client.join(joinRequest).get
     } 
  }
  
  override def send(getMembersRequest: GetMembersRequest): Try[GetMembersResponse] = {
    Try {
       client.getMembers().get
     } 
  }
  

}

object NoRetry extends RetryPolicy[com.twitter.util.Try[Nothing]] {
       def apply(e: com.twitter.util.Try[Nothing]) = {
          None
      }
}
