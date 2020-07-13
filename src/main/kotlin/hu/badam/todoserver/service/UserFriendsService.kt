package hu.badam.todoserver.service

import hu.badam.todoserver.model.Task
import hu.badam.todoserver.repository.FriendRepository
import hu.badam.todoserver.repository.TaskRepository
import hu.badam.todoserver.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserFriendsService {
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userFriendRepository: FriendRepository
    @Autowired
    private lateinit var taskRepository: TaskRepository

    fun sendFriendRequest(fromEmail: String, toEmail: String) {
        val from = userRepository.findFirstByEmail(fromEmail) ?: throw IllegalArgumentException("Invalid from email")
        val to = userFriendRepository.findByUser_Email(toEmail) ?: throw IllegalArgumentException("Invalid to email")
        to.friendRequests.add(from)
        userFriendRepository.save(to)
    }

    fun acceptFriendRequest (userEmail: String, requesterEmail: String) {
        val user = userFriendRepository.findByUser_Email(userEmail) ?: throw IllegalArgumentException("Invalid user email")

        val validRequest = user.friendRequests.removeIf { it.email == requesterEmail }
        if (!validRequest) throw java.lang.IllegalArgumentException("This user didn't send a friend request")

        val requester = userRepository.findFirstByEmail(requesterEmail) ?: throw IllegalArgumentException("Invalid requester email")
        user.friends.add(requester)

        userFriendRepository.save(user)
    }

    fun declineFriendRequest (userEmail: String, requesterEmail: String) {
        val user = userFriendRepository.findByUser_Email(userEmail) ?: throw IllegalArgumentException("Invalid user email")

        val validRequest = user.friendRequests.removeIf { it.email == requesterEmail }
        if (!validRequest) throw java.lang.IllegalArgumentException("This user didn't send a friend request")

        userFriendRepository.save(user)
    }

    fun getTasks(email: String, friend: String, from: Date?): List<Task> {
        val user = userFriendRepository.findByUser_Email(email) ?: throw IllegalArgumentException("Invalid user email")
        user.friends.find { it.email == friend } ?: throw IllegalArgumentException("The requested user does not your friend")
        return taskRepository.findAllAssigned(friend, from)
    }
}