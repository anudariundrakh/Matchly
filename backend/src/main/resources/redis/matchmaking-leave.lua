local waitingQueue = KEYS[1]

local userId = ARGV[1]
local roomPrefix = ARGV[2]
local partnerPrefix = ARGV[3]

local userRoomKey =
        roomPrefix .. userId

local userPartnerKey =
        partnerPrefix .. userId

redis.call(
    "LREM",
    waitingQueue,
    0,
    userId
)

local currentRoom =
        redis.call(
            "GET",
            userRoomKey
        )

local partnerUserId =
        redis.call(
            "GET",
            userPartnerKey
        )

redis.call(
    "DEL",
    userRoomKey
)

redis.call(
    "DEL",
    userPartnerKey
)

local disconnectedPartner = ""

if partnerUserId then
    local partnerRoomKey =
            roomPrefix .. partnerUserId

    local partnerPartnerKey =
            partnerPrefix .. partnerUserId

    local partnerRoom =
            redis.call(
                "GET",
                partnerRoomKey
            )

    local partnerPartner =
            redis.call(
                "GET",
                partnerPartnerKey
            )

    if partnerPartner == userId
            and partnerRoom == currentRoom then

        redis.call(
            "DEL",
            partnerRoomKey
        )

        redis.call(
            "DEL",
            partnerPartnerKey
        )

        disconnectedPartner =
                partnerUserId
    end
end

return (currentRoom or "")
        .. "|"
        .. disconnectedPartner