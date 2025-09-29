// MongoDB script to insert sample data for testing search functionality
// Run this in MongoDB shell or MongoDB Compass

// Connect to your database
use your_db_name;

// Insert sample categories
db.categories.insertMany([
    {
        "_id": "gaming",
        "slug": "gaming",
        "name": "Gaming",
        "description": "Gaming products including accounts, items, and currencies",
        "parentId": null,
        "ancestors": [],
        "path": "gaming",
        "sortOrder": 1,
        "status": "ACTIVE",
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "mmorpg",
        "slug": "mmorpg", 
        "name": "MMORPG",
        "description": "Massively Multiplayer Online Role-Playing Games",
        "parentId": "gaming",
        "ancestors": ["gaming"],
        "path": "gaming/mmorpg",
        "sortOrder": 1,
        "status": "ACTIVE",
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "fps",
        "slug": "fps",
        "name": "FPS Games",
        "description": "First Person Shooter games",
        "parentId": "gaming",
        "ancestors": ["gaming"],
        "path": "gaming/fps",
        "sortOrder": 2,
        "status": "ACTIVE",
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "digital-software",
        "slug": "digital-software",
        "name": "Digital Software",
        "description": "Digital software and applications",
        "parentId": null,
        "ancestors": [],
        "path": "digital-software",
        "sortOrder": 2,
        "status": "ACTIVE",
        "createdAt": new Date(),
        "updatedAt": new Date()
    }
]);

// Insert sample products
db.products.insertMany([
    {
        "_id": "prod-minecraft-premium",
        "userId": "admin",
        "slug": "minecraft-premium-account",
        "name": "Minecraft Premium Account",
        "description": "Original Minecraft Premium Account with full access to all features. Includes multiplayer access and official server support.",
        "active": "ACTIVE",
        "categoryIds": ["gaming", "digital-software"],
        "images": [
            {
                "url": "/uploads/products/minecraft-premium.jpg",
                "alt": "Minecraft Premium Account",
                "isPrimary": true
            }
        ],
        "attributes": {
            "platform": "pc",
            "type": "account",
            "region": "global"
        },
        "basePrice": NumberDecimal("29.99"),
        "currency": "USD",
        "tags": ["minecraft", "premium", "account", "popular", "sandbox"],
        "ratingAvg": 4.8,
        "ratingCount": 1250,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-wow-gold-1000",
        "userId": "admin",
        "slug": "wow-gold-1000-pieces",
        "name": "World of Warcraft Gold - 1000 Pieces",
        "description": "High quality WoW gold for all servers. Fast delivery within 15 minutes. Safe and secure transaction guaranteed.",
        "active": "ACTIVE",
        "categoryIds": ["gaming", "mmorpg"],
        "images": [
            {
                "url": "/uploads/products/wow-gold.jpg", 
                "alt": "WoW Gold",
                "isPrimary": true
            }
        ],
        "attributes": {
            "game": "world-of-warcraft",
            "server": "all-servers",
            "type": "currency"
        },
        "basePrice": NumberDecimal("15.99"),
        "currency": "USD",
        "tags": ["wow", "gold", "currency", "mmorpg", "fast-delivery"],
        "ratingAvg": 4.6,
        "ratingCount": 890,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-cs-go-skins-ak47",
        "userId": "admin",
        "slug": "csgo-ak47-redline-skin",
        "name": "CS:GO AK-47 Redline Skin (Field-Tested)",
        "description": "Popular AK-47 Redline skin in Field-Tested condition. Great for Counter-Strike Global Offensive players.",
        "active": "ACTIVE",
        "categoryIds": ["gaming", "fps"],
        "images": [
            {
                "url": "/uploads/products/ak47-redline.jpg",
                "alt": "AK-47 Redline Skin",
                "isPrimary": true
            }
        ],
        "attributes": {
            "game": "csgo",
            "weapon": "ak-47",
            "condition": "field-tested",
            "type": "skin"
        },
        "basePrice": NumberDecimal("45.00"),
        "currency": "USD",
        "tags": ["csgo", "skin", "ak-47", "redline", "fps"],
        "ratingAvg": 4.7,
        "ratingCount": 456,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-steam-wallet-50",
        "userId": "admin",
        "slug": "steam-wallet-code-50-usd",
        "name": "Steam Wallet Code - $50 USD",
        "description": "Steam Wallet gift card worth $50 USD. Perfect for purchasing games and in-game content on Steam platform.",
        "active": "ACTIVE",
        "categoryIds": ["gaming", "digital-software"],
        "images": [
            {
                "url": "/uploads/products/steam-wallet-50.jpg",
                "alt": "Steam Wallet $50",
                "isPrimary": true
            }
        ],
        "attributes": {
            "platform": "steam",
            "value": "50",
            "type": "gift-card",
            "region": "us"
        },
        "basePrice": NumberDecimal("50.00"),
        "currency": "USD",
        "tags": ["steam", "wallet", "gift-card", "gaming"],
        "ratingAvg": 4.9,
        "ratingCount": 2100,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-minecraft-mods-collection",
        "userId": "admin",
        "slug": "minecraft-mods-collection-premium",
        "name": "Minecraft Mods Collection - Premium Pack",
        "description": "Comprehensive collection of the most popular Minecraft mods. Includes installation guide and support.",
        "active": "ACTIVE",
        "categoryIds": ["gaming"],
        "images": [
            {
                "url": "/uploads/products/minecraft-mods.jpg",
                "alt": "Minecraft Mods Collection",
                "isPrimary": true
            }
        ],
        "attributes": {
            "platform": "pc",
            "game": "minecraft",
            "type": "mods",
            "count": "50+"
        },
        "basePrice": NumberDecimal("19.99"),
        "currency": "USD",
        "tags": ["minecraft", "mods", "collection", "premium"],
        "ratingAvg": 4.5,
        "ratingCount": 678,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-discord-nitro-1month",
        "userId": "admin",
        "slug": "discord-nitro-1-month-subscription",
        "name": "Discord Nitro - 1 Month Subscription",
        "description": "Discord Nitro subscription for 1 month. Enjoy enhanced chat features, higher quality streaming, and custom emojis.",
        "active": "ACTIVE",
        "categoryIds": ["digital-software"],
        "images": [
            {
                "url": "/uploads/products/discord-nitro.jpg",
                "alt": "Discord Nitro",
                "isPrimary": true
            }
        ],
        "attributes": {
            "platform": "discord",
            "duration": "1-month",
            "type": "subscription"
        },
        "basePrice": NumberDecimal("9.99"),
        "currency": "USD",
        "tags": ["discord", "nitro", "subscription", "communication"],
        "ratingAvg": 4.4,
        "ratingCount": 345,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    }
]);

// Create some additional sample data for better search testing
db.products.insertMany([
    {
        "_id": "prod-minecraft-server-hosting",
        "userId": "admin",
        "slug": "minecraft-server-hosting-premium",
        "name": "Minecraft Server Hosting - Premium Plan",
        "description": "Professional Minecraft server hosting with 24/7 support, automatic backups, and DDoS protection.",
        "active": "ACTIVE",
        "categoryIds": ["gaming", "digital-software"],
        "attributes": {
            "platform": "minecraft",
            "type": "hosting",
            "players": "up-to-100",
            "support": "24-7"
        },
        "basePrice": NumberDecimal("25.00"),
        "currency": "USD",
        "tags": ["minecraft", "server", "hosting", "premium", "multiplayer"],
        "ratingAvg": 4.6,
        "ratingCount": 234,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    },
    {
        "_id": "prod-gaming-mouse-rgb",
        "userId": "admin",
        "slug": "gaming-mouse-rgb-pro",
        "name": "Gaming Mouse RGB Pro",
        "description": "High precision gaming mouse with RGB lighting and programmable buttons. Perfect for FPS and MMORPG games.",
        "active": "ACTIVE",
        "categoryIds": ["gaming"],
        "attributes": {
            "type": "hardware",
            "rgb": "yes",
            "dpi": "16000",
            "buttons": "12"
        },
        "basePrice": NumberDecimal("79.99"),
        "currency": "USD",
        "tags": ["gaming", "mouse", "rgb", "hardware", "pro"],
        "ratingAvg": 4.3,
        "ratingCount": 567,
        "softDeletedAt": null,
        "createdAt": new Date(),
        "updatedAt": new Date()
    }
]);

// Verify data was inserted
print("=== Sample Data Insertion Complete ===");
print("Categories inserted: " + db.categories.countDocuments());
print("Products inserted: " + db.products.countDocuments());

// Test some search queries
print("\n=== Testing Search Queries ===");

print("\n1. Text search for 'minecraft':");
db.products.find(
    { $text: { $search: "minecraft" } },
    { score: { $meta: "textScore" }, name: 1, basePrice: 1 }
).sort({ score: { $meta: "textScore" } }).forEach(doc => {
    print(`- ${doc.name}: $${doc.basePrice} (score: ${doc.score})`);
});

print("\n2. Category filter for 'gaming':");
db.products.find(
    { categoryIds: "gaming", active: "ACTIVE" },
    { name: 1, basePrice: 1 }
).forEach(doc => {
    print(`- ${doc.name}: $${doc.basePrice}`);
});

print("\n3. Price range filter ($10-$30):");
db.products.find(
    { 
        basePrice: { $gte: NumberDecimal("10.00"), $lte: NumberDecimal("30.00") },
        active: "ACTIVE"
    },
    { name: 1, basePrice: 1 }
).sort({ basePrice: 1 }).forEach(doc => {
    print(`- ${doc.name}: $${doc.basePrice}`);
});

print("\n4. Combined search (text + filters):");
db.products.find(
    {
        $text: { $search: "gaming minecraft" },
        basePrice: { $lte: NumberDecimal("50.00") },
        active: "ACTIVE"
    },
    { score: { $meta: "textScore" }, name: 1, basePrice: 1 }
).sort({ score: { $meta: "textScore" } }).forEach(doc => {
    print(`- ${doc.name}: $${doc.basePrice} (score: ${doc.score})`);
});

print("\nSample data setup completed successfully!");