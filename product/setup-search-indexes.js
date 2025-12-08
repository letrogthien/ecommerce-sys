// MongoDB script to create text indexes for search functionality
// Run this in MongoDB shell or MongoDB Compass

// Connect to your database (replace 'your_db_name' with actual database name)
use your_db_name;

// 1. Create text index for products collection
db.products.createIndex(
    {
        "name": "text",
        "description": "text", 
        "tags": "text"
    },
    {
        name: "product_text_index",
        weights: {
            "name": 10,
            "description": 3,
            "tags": 2
        },
        default_language: "english",
        language_override: "language"
    }
);

// 2. Create text index for categories collection
db.categories.createIndex(
    {
        "name": "text",
        "description": "text",
        "path": "text"
    },
    {
        name: "category_text_index", 
        weights: {
            "name": 10,
            "description": 5,
            "path": 2
        },
        default_language: "english"
    }
);

// 3. Create additional indexes for better search performance

// Product indexes
db.products.createIndex({ "active": 1, "softDeletedAt": 1 }, { name: "product_active_index" });
db.products.createIndex({ "categoryIds": 1 }, { name: "product_category_index" });
db.products.createIndex({ "basePrice": 1 }, { name: "product_price_index" });
db.products.createIndex({ "ratingAvg": -1 }, { name: "product_rating_index" });
db.products.createIndex({ "tags": 1 }, { name: "product_tags_index" });
db.products.createIndex({ "userId": 1 }, { name: "product_user_index" });

// Compound indexes for common filter combinations
db.products.createIndex(
    { 
        "active": 1, 
        "categoryIds": 1, 
        "basePrice": 1 
    }, 
    { name: "product_filter_compound_index" }
);

db.products.createIndex(
    { 
        "active": 1, 
        "ratingAvg": -1, 
        "createdAt": -1 
    }, 
    { name: "product_rating_date_index" }
);

// Category indexes
db.categories.createIndex({ "status": 1, "sortOrder": 1 }, { name: "category_status_sort_index" });
db.categories.createIndex({ "parentId": 1, "sortOrder": 1 }, { name: "category_hierarchy_index" });
db.categories.createIndex({ "path": 1 }, { name: "category_path_index" });
db.categories.createIndex({ "slug": 1 }, { name: "category_slug_index", unique: true });

// 4. Verify indexes were created
print("=== Product Collection Indexes ===");
db.products.getIndexes().forEach(index => {
    print(`${index.name}: ${JSON.stringify(index.key)}`);
});

print("\n=== Category Collection Indexes ===");
db.categories.getIndexes().forEach(index => {
    print(`${index.name}: ${JSON.stringify(index.key)}`);
});

// 5. Sample aggregation queries for testing

print("\n=== Sample Search Queries ===");

// Text search example
print("1. Text search for products containing 'minecraft':");
db.products.find(
    { $text: { $search: "minecraft" } },
    { score: { $meta: "textScore" } }
).sort({ score: { $meta: "textScore" } }).limit(5).pretty();

// Complex search with filters
print("\n2. Complex search with price and category filters:");
db.products.aggregate([
    {
        $match: {
            $text: { $search: "game" },
            active: "ACTIVE",
            basePrice: { $gte: 10, $lte: 100 },
            categoryIds: { $in: ["gaming", "software"] }
        }
    },
    {
        $addFields: {
            score: { $meta: "textScore" }
        }
    },
    {
        $sort: { score: -1, createdAt: -1 }
    },
    {
        $limit: 10
    },
    {
        $project: {
            name: 1,
            basePrice: 1,
            ratingAvg: 1,
            score: 1
        }
    }
]).pretty();

// Category search example  
print("\n3. Category text search:");
db.categories.find(
    { $text: { $search: "gaming electronics" } },
    { score: { $meta: "textScore" } }
).sort({ score: { $meta: "textScore" } }).limit(5).pretty();

print("\nSearch indexes setup completed successfully!");