local waitingQueue = KEYS[1]

local userId = ARGV[1]
local roomPrefix = ARGV[2]
local partnerPrefix = ARGV[3]
local newRoomId = ARGV[4]
local ttlSeconds = tonumber(ARGV[5])

local currentRoomKey = roomPrefix .. userId
local currentPartnerKey = partnerPrefix .. userId

local existingRoom = redis.call(
    "GET",
    currentRoomKey
)

if existingRoom then
    local existingPartner = redis.call(
        "GET",
        currentPartnerKey
    )

    return "MATCHED|" ..
        existingRoom ..
        "|" ..
        (existingPartner or "")
end

redis.call(
    "LREM",
    waitingQueue,
    0,
    userId
)

local waitingUserId = nil

while true do
    waitingUserId = redis.call(
        "LPOP",
        waitingQueue
    )

    if not waitingUserId then
        break
    end

    local waitingUserRoom = redis.call(
        "GET",
        roomPrefix .. waitingUserId
    )

    if not waitingUserRoom then
        break
    end
end

if not waitingUserId then
    redis.call(
        "RPUSH",
        waitingQueue,
        userId
    )

    return "WAITING||"
end

redis.call(
    "SETEX",
    currentRoomKey,
    ttlSeconds,
    newRoomId
)

redis.call(
    "SETEX",
    currentPartnerKey,
    ttlSeconds,
    waitingUserId
)

redis.call(
    "SETEX",
    roomPrefix .. waitingUserId,
    ttlSeconds,
    newRoomId
)

redis.call(
    "SETEX",
    partnerPrefix .. waitingUserId,
    ttlSeconds,
    userId
)

return "MATCHED|" ..
    newRoomId ..
    "|" ..
    waitingUserId