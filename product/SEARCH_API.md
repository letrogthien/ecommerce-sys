# Search API Documentation

## Overview
Hệ thống search được triển khai sử dụng MongoDB Text Index và Aggregation Framework để cung cấp tính năng tìm kiếm mạnh mẽ cho products và categories.

## Features
- Full-text search với weighted scoring
- Advanced filtering (price range, categories, ratings, attributes)
- Faceted search với aggregation
- Autocomplete suggestions
- Pagination support
- Multi-field sorting

## API Endpoints

### 1. Product Search

#### POST /api/v1/product-service/search/products
Tìm kiếm products với advanced filtering (recommended)

**Request Body:**
```json
{
  "keyword": "minecraft gaming",
  "categoryIds": ["gaming", "software"],
  "minPrice": 10.0,
  "maxPrice": 100.0,
  "tags": ["popular", "multiplayer"],
  "attributes": {
    "platform": "pc",
    "genre": "adventure"
  },
  "minRating": 4.0,
  "maxRating": 5.0,
  "sortBy": "relevance",
  "sortDirection": "DESC",
  "page": 0,
  "size": 20,
  "includeInactive": false
}
```

**Response:**
```json
{
  "data": {
    "items": [
      {
        "id": "product123",
        "name": "Minecraft Premium Account",
        "description": "...",
        "basePrice": 29.99,
        "ratingAvg": 4.8,
        "...": "..."
      }
    ],
    "totalItems": 156,
    "currentPage": 0,
    "pageSize": 20,
    "totalPages": 8,
    "executionTimeMs": 45
  }
}
```

#### GET /api/v1/product-service/search/products
Tìm kiếm products với query parameters (simple search)

**Query Parameters:**
- `keyword` (string): Từ khóa tìm kiếm
- `categoryIds` (array): IDs của categories để filter
- `minPrice`, `maxPrice` (decimal): Khoảng giá
- `minRating`, `maxRating` (double): Khoảng rating
- `tags` (array): Tags để filter
- `sortBy` (string): Trường để sort (relevance, price, rating, createdAt)
- `sortDirection` (string): Hướng sort (ASC, DESC)
- `page`, `size` (int): Pagination

**Example:**
```
GET /api/v1/product-service/search/products?keyword=minecraft&minPrice=10&maxPrice=50&page=0&size=20
```

### 2. Category Search

#### GET /api/v1/product-service/search/categories
Tìm kiếm categories

**Query Parameters:**
- `keyword` (string): Từ khóa tìm kiếm
- `page`, `size` (int): Pagination
- `includeInactive` (boolean): Bao gồm inactive categories

**Example:**
```
GET /api/v1/product-service/search/categories?keyword=gaming&page=0&size=10
```

### 3. Search Facets

#### POST /api/v1/product-service/search/facets
Lấy facets cho advanced filtering

**Request Body:** Same as product search request

**Response:**
```json
{
  "data": {
    "categories": [
      {
        "value": "gaming",
        "displayName": "Gaming",
        "count": 150
      }
    ],
    "priceRanges": [
      {
        "value": "0-100000",
        "displayName": "Under $100",
        "count": 45
      }
    ],
    "tags": [...],
    "attributes": {
      "platform": [
        {
          "value": "pc",
          "displayName": "PC",
          "count": 89
        }
      ]
    },
    "ratingRanges": [...]
  }
}
```

### 4. Autocomplete Suggestions

#### GET /api/v1/product-service/search/suggestions/products
Lấy product suggestions cho autocomplete

**Query Parameters:**
- `q` (string, required): Partial text input
- `limit` (int): Maximum suggestions (default: 10, max: 20)

**Example:**
```
GET /api/v1/product-service/search/suggestions/products?q=minecr&limit=5
```

**Response:**
```json
{
  "data": [
    "Minecraft Premium Account",
    "Minecraft Mods Collection", 
    "Minecraft Server Hosting"
  ]
}
```

#### GET /api/v1/product-service/search/suggestions/categories
Lấy category suggestions cho autocomplete

### 5. Popular Search Terms

#### GET /api/v1/product-service/search/popular-terms
Lấy popular search terms

**Query Parameters:**
- `limit` (int): Maximum terms (default: 10)

## MongoDB Text Index Setup

### Automatic Setup
Các text indexes sẽ được tự động tạo khi application start qua `MongoSearchIndexConfig`.

### Manual Setup
Chạy script `setup-search-indexes.js` trong MongoDB shell:
```bash
mongo your_database_name setup-search-indexes.js
```

### Text Index Structure

**Products Collection:**
```javascript
{
  "name": "text",      // weight: 10
  "description": "text", // weight: 3  
  "tags": "text"       // weight: 2
}
```

**Categories Collection:**
```javascript
{
  "name": "text",        // weight: 10
  "description": "text", // weight: 5
  "path": "text"         // weight: 2
}
```

## Search Query Examples

### 1. Basic Text Search
```javascript
db.products.find(
  { $text: { $search: "minecraft gaming" } },
  { score: { $meta: "textScore" } }
).sort({ score: { $meta: "textScore" } })
```

### 2. Advanced Search with Filters
```javascript
db.products.aggregate([
  {
    $match: {
      $text: { $search: "minecraft" },
      active: "ACTIVE",
      basePrice: { $gte: 10, $lte: 100 },
      categoryIds: { $in: ["gaming"] },
      ratingAvg: { $gte: 4.0 }
    }
  },
  {
    $addFields: {
      score: { $meta: "textScore" }
    }
  },
  {
    $sort: { score: -1, ratingAvg: -1 }
  }
])
```

### 3. Faceted Search
```javascript
db.products.aggregate([
  { $match: { $text: { $search: "gaming" } } },
  {
    $facet: {
      "categories": [
        { $unwind: "$categoryIds" },
        { $group: { _id: "$categoryIds", count: { $sum: 1 } } }
      ],
      "priceRanges": [
        {
          $bucket: {
            groupBy: "$basePrice",
            boundaries: [0, 50, 100, 200, 500],
            default: "Other"
          }
        }
      ]
    }
  }
])
```

## Performance Optimization

### 1. Indexes
- Text indexes cho full-text search
- Compound indexes cho common filter combinations
- Individual indexes cho các trường thường filter

### 2. Query Optimization
- Sử dụng projection để limit fields trả về
- Implement pagination properly
- Cache popular searches

### 3. Caching Strategy
- Cache search facets (TTL: 5 minutes)
- Cache autocomplete suggestions (TTL: 10 minutes)
- Cache popular search terms (TTL: 1 hour)

## Error Handling
- Invalid search requests return empty results with appropriate HTTP status
- Search timeouts handled gracefully
- Index creation errors logged but don't stop application

## Monitoring
- Log search queries cho analytics
- Track search performance metrics
- Monitor index usage và optimization opportunities