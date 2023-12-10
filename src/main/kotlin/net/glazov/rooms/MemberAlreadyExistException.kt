package net.glazov.rooms

class MemberAlreadyExistException: Exception(
    "There is already a member with that memberId in the room"
)