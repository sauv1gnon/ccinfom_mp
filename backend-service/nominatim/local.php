<?php
// Production-optimized Nominatim configuration for CCInfoMS geoservices
// Enhanced for delivery application integration with improved performance and security

if (!isset($GLOBALS['CONF'])) {
    $GLOBALS['CONF'] = [];
}

// ================== CORS CONFIGURATION ==================
$GLOBALS['CONF']['CORS'] = [
    'enabled' => true,
    'origins' => ['*'],
    'methods' => ['GET', 'POST', 'OPTIONS'],
    'headers' => ['Content-Type', 'Accept'],
    'credentials' => false
];

// ================== HTTP CACHING ==================
$GLOBALS['CONF']['HTTP'] = $GLOBALS['CONF']['HTTP'] ?? [];
$GLOBALS['CONF']['HTTP']['Cache-Control'] = 'public, max-age=3600';

// ================== DATABASE PERFORMANCE ==================
$GLOBALS['CONF']['database'] = [
    'init_methods' => ['load_ext']
];

// ================== IMPORT SETTINGS ==================
$GLOBALS['CONF']['import'] = [
    'threads' => 4,
    'use_way_point_indexing' => true,
    'enable_geometry_index' => true,
    'import_wikipedia' => false
];

// ================== SEARCH CONFIGURATION ==================
$GLOBALS['CONF']['search'] = [
    'admin_only' => false,
    'country_codes' => ['ph'],
    'limit' => 10,
    'addressdetails' => true,
    'dedupe' => true
];

// ================== REVERSE GEOCODING ==================
$GLOBALS['CONF']['reverse'] = [
    'limit' => 1,
    'addressdetails' => true,
    'zoom' => 18
];

// ================== LOGGING ==================
$GLOBALS['CONF']['log_level'] = 'WARNING';

// ================== OUTPUT FORMAT ==================
$GLOBALS['CONF']['output'] = [
    'format' => 'json',
    'encoding' => 'utf-8'
];

$GLOBALS['CONF']['debug'] = false;
