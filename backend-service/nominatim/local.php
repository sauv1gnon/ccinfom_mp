<?php
// Production-optimized Nominatim configuration for CCInfoMS geoservices
// Enhanced for delivery application integration with improved performance and security

if (!isset($GLOBALS['CONF'])) {
    $GLOBALS['CONF'] = [];
}

// ================== CORS CONFIGURATION ==================
// Production-ready CORS settings for multiple frontend applications
$GLOBALS['CONF']['CORS'] = [
    'enabled' => true,
    'origins' => [
        'http://localhost:3000',
        'http://localhost:3001', 
        'http://localhost:3002',
        'http://localhost:8080',
        'http://localhost:8081',
        'http://localhost:8082',
        'https://admin.localhost',
        'https://customer.localhost', 
        'https://driver.localhost',
        'https://api.localhost',
        '*' // Allow all origins in production - configure appropriately
    ],
    'methods' => ['GET', 'POST', 'OPTIONS'],
    'headers' => [
        'Authorization',
        'Content-Type',
        'X-Requested-With',
        'Accept',
        'Accept-Language',
        'Content-Language',
        'Accept-Encoding'
    ],
    'credentials' => false,
    'max_age' => 86400 // 24 hours
];

// ================== HTTP CACHING ==================
// Optimized caching headers for production performance
$GLOBALS['CONF']['HTTP'] = $GLOBALS['CONF']['HTTP'] ?? [];
$GLOBALS['CONF']['HTTP']['Cache-Control'] = 'public, max-age=86400'; // 24 hours
$GLOBALS['CONF']['HTTP']['Vary'] = 'Accept-Encoding';
$GLOBALS['CONF']['HTTP']['ETag'] = true;

// ================== RATE LIMITING ==================
// Production rate limiting configuration
$GLOBALS['CONF']['rate_limit'] = [
    'enabled' => true,
    'points' => 100, // requests per interval
    'duration' => 60, // seconds
    'block_duration' => 300 // 5 minutes
];

// ================== SECURITY HEADERS ==================
// Security headers for production deployment
$GLOBALS['CONF']['security_headers'] = [
    'X-Content-Type-Options' => 'nosniff',
    'X-Frame-Options' => 'DENY',
    'X-XSS-Protection' => '1; mode=block',
    'Referrer-Policy' => 'strict-origin-when-cross-origin',
    'Content-Security-Policy' => "default-src 'self'; img-src 'self' data: https:; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'",
    'Strict-Transport-Security' => 'max-age=31536000; includeSubDomains; preload'
];

// ================== DATABASE PERFORMANCE ==================
// Optimized database settings for production
$GLOBALS['CONF']['database'] = [
    'init_methods' => ['load_ext'],
    'max_connections' => 200,
    'shared_buffers' => '2GB',
    'work_mem' => '256MB',
    'maintenance_work_mem' => '1GB',
    'effective_cache_size' => '6GB'
];

// ================== IMPORT SETTINGS ==================
// Enhanced import configuration for Philippines data
$GLOBALS['CONF']['import'] = [
    'threads' => 8, // Match CPU cores
    'use_way_point_indexing' => true,
    'enable_geometry_index' => true,
    'enable_osmosis_polygon_index' => true,
    'create_search_indices' => true,
    'create_postcode_gazetteer' => true,
    'create_word_index' => true,
    'import_wikipedia' => false, // Disabled for faster import
    'import_natsort' => true
];

// ================== SEARCH CONFIGURATION ==================
// Production search optimization
$GLOBALS['CONF']['search'] = [
    'admin_only' => false,
    'country_codes' => ['ph'], // Philippines priority
    'exclude_place_ids' => [],
    'limit' => 25,
    'addressdetails' => true,
    'extratags' => true,
    'dedupe' => true,
    'bounded' => false,
    'viewbox' => null,
    'polygon_geojson' => false,
    'polygon_kml' => false,
    'polygon_svg' => false,
    'polygon_text' => false
];

// ================== REVERSE GEOCODING ==================
// Optimized reverse geocoding for mobile applications
$GLOBALS['CONF']['reverse'] = [
    'limit' => 1,
    'addressdetails' => true,
    'extratags' => true,
    'namedetails' => true,
    'zoom' => 18
];

// ================== LOGGING ==================
// Enhanced logging for production monitoring
$GLOBALS['CONF']['log_level'] = 'INFO';
$GLOBALS['CONF']['log_format'] = 'json';
$GLOBALS['CONF']['log_file'] = '/var/log/nominatim/nominatim.log';

// ================== SPECIAL PHRASES ==================
// Philippines-specific search optimizations
$GLOBALS['CONF']['special_phrases'] = [
    'en' => [
        'pharmacy' => 'amenity=pharmacy',
        'hospital' => 'amenity=hospital',
        'police' => 'amenity=police',
        'post_office' => 'amenity=post_office',
        'bank' => 'amenity=bank',
        'atm' => 'amenity=atm'
    ]
];

// ================== GEOCODING THRESHOLDS ==================
// Performance tuning for better search results
$GLOBALS['CONF']['geocoding'] = [
    'query_regex' => '/[a-zA-Z]{1}/',
    'min_word_length' => 2,
    'word_cutoff' => 0.66,
    'final_endpoint' => 'json',
    'class_factor' => 1,
    'address_factor' => 1,
    'population_factor' => 0.5,
    'importance_factor' => 0.8
];

// ================== OUTPUT FORMAT ==================
// Default output configuration
$GLOBALS['CONF']['output'] = [
    'format' => 'json',
    'encoding' => 'utf-8',
    'pretty_print' => false,
    'geom_format' => 'wkt'
];

// ================== API LIMITATIONS ==================
// Production API limits
$GLOBALS['CONF']['api_limits'] = [
    'max_results_per_page' => 50,
    'default_results_per_page' => 10,
    'max_candidates' => 1000,
    'max_geometry_points' => 2000
];

// ================== ERROR HANDLING ==================
// Production error handling
$GLOBALS['CONF']['error_handling'] = [
    'include_error_details' => false,
    'log_errors' => true,
    'show_sql_errors' => false
];

// Override any upstream defaults with our production settings
$GLOBALS['CONF']['debug'] = false;
$GLOBALS['CONF']['show_query_urls'] = false;
$GLOBALS['CONF']['show_usage_stats'] = true;
