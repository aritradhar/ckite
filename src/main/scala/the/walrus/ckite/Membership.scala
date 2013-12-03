package the.walrus.ckite

import java.util.HashMap

trait Membership {

  def allMembers: Seq[Member]

  def reachMajority(votes: Seq[Member]): Boolean

  def majority: String

  def majoritiesCount: Int

  def majoritiesMap: java.util.Map[Seq[Member], Int]

}

class StableMembership(members: Seq[Member]) extends Membership {

  override def allMembers = members

  override def reachMajority(votes: Seq[Member]): Boolean = {
    votes.size >= internalMajority
  }

  def internalMajority = ((members.size + 1) / 2) + 1

  override def majority: String = s"simple majority of ${internalMajority}"

  override def majoritiesCount = 1

  override def majoritiesMap: java.util.Map[Seq[Member], Int] = {
    val map = new HashMap[Seq[Member], Int]()
    map.put(members, (internalMajority - 1))
    map
  }
  
  override def toString(): String = {
    allMembers.map {m => m.id }.mkString(",")
  }

}

class CompoundMembership(oldMembership: Membership, newMembership: Membership) extends Membership {

  override def allMembers = (oldMembership.allMembers.toSet ++ newMembership.allMembers.toSet).toSet.toSeq

  override def reachMajority(votes: Seq[Member]): Boolean = {
    oldMembership.reachMajority(votes) && newMembership.reachMajority(votes)
  }

  override def majority: String = s"compund majority of [${oldMembership.majority},${newMembership.majority}]"

  override def majoritiesCount = 2

  override def majoritiesMap: java.util.Map[Seq[Member], Int] = {
    val map = oldMembership.majoritiesMap
    map.putAll(newMembership.majoritiesMap)
    map
  }

  override def toString(): String = {
    allMembers.map {m => m.id }.mkString(",")
  }
}