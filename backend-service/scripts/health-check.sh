#!/bin/bash
# CCInfoMS Geoservices Health Check Script
# Comprehensive health monitoring for all geoservices components

set -e

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Load environment variables
load_environment() {
    if [ -f "$ENV_FILE" ]; then
        export $(grep -v '^#' "$ENV_FILE" | xargs)
    fi
}

# Get service ports
ORS_PORT=${ORS_HTTP_PORT:-8080}
NOMINATIM_PORT=${NOMINATIM_HTTP_PORT:-8081}
TILESERVER_PORT=${TILESERVER_HTTP_PORT:-8082}

# Check function
check_service() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "Checking $service_name... "
    
    if curl -f -s -o /dev/null -w "%{http_code}" "$url" | grep -q "$expected_status"; then
        echo -e "${GREEN}✓ HEALTHY${NC}"
        return 0
    else
        echo -e "${RED}✗ UNHEALTHY${NC}"
        return 1
    fi
}

# Check ORS service
check_ors() {
    echo -e "\n${BLUE}=== ORS (OpenRouteService) Health Check ===${NC}"
    
    # Health endpoint
    check_service "ORS Health" "http://localhost:$ORS_PORT/ors/v2/health"
    
    # Test routing API
    echo -n "Testing ORS Routing API... "
    if curl -s -X POST "http://localhost:$ORS_PORT/ors/v2/directions/foot-walking" \
        -H "Content-Type: application/json" \
        -d '{"coordinates":[[14.5995,120.9842],[14.6760,121.0437]]}' | grep -q "routes"; then
        echo -e "${GREEN}✓ ROUTING API WORKING${NC}"
    else
        echo -e "${RED}✗ ROUTING API FAILED${NC}"
    fi
    
    # Check graphs directory
    if [ -d "$PROJECT_ROOT/ors/graphs" ] && [ "$(ls -A $PROJECT_ROOT/ors/graphs)" ]; then
        echo -e "${GREEN}✓ Graph files exist${NC}"
    else
        echo -e "${YELLOW}⚠ No graph files found${NC}"
    fi
}

# Check Nominatim service
check_nominatim() {
    echo -e "\n${BLUE}=== Nominatim Health Check ===${NC}"
    
    # Status endpoint
    check_service "Nominatim Status" "http://localhost:$NOMINATIM_PORT/status.php"
    
    # Test search API
    echo -n "Testing Nominatim Search API... "
    if curl -s "http://localhost:$NOMINATIM_PORT/search?q=Manila&format=json&limit=1" | grep -q "Manila"; then
        echo -e "${GREEN}✓ SEARCH API WORKING${NC}"
    else
        echo -e "${RED}✗ SEARCH API FAILED${NC}"
    fi
    
    # Test reverse geocoding
    echo -n "Testing Reverse Geocoding... "
    if curl -s "http://localhost:$NOMINATIM_PORT/reverse?lat=14.5995&lon=120.9842&format=json" | grep -q "Manila"; then
        echo -e "${GREEN}✓ REVERSE GEOCODING WORKING${NC}"
    else
        echo -e "${YELLOW}⚠ Reverse geocoding may be slow (data still importing)${NC}"
    fi
}

# Check Tileserver service
check_tileserver() {
    echo -e "\n${BLUE}=== Tileserver Health Check ===${NC}"
    
    # Main endpoint
    check_service "Tileserver" "http://localhost:$TILESERVER_PORT/"
    
    # Test tile endpoint
    echo -n "Testing Tile API... "
    if curl -s -I "http://localhost:$TILESERVER_PORT/styles/basic/0/0/0.png" | grep -q "200 OK"; then
        echo -e "${GREEN}✓ TILE API WORKING${NC}"
    else
        echo -e "${RED}✗ TILE API FAILED${NC}"
    fi
    
    # Check MBTiles file
    if [ -f "$PROJECT_ROOT/data/philippines.mbtiles" ]; then
        echo -e "${GREEN}✓ MBTiles file exists${NC}"
    else
        echo -e "${RED}✗ MBTiles file missing${NC}"
    fi
}

# Check Docker containers
check_containers() {
    echo -e "\n${BLUE}=== Docker Container Status ===${NC}"
    
    # Check if containers are running
    containers=("ors-app" "nominatim" "tileserver")
    
    for container in "${containers[@]}"; do
        if docker ps --filter "name=$container" --format "{{.Names}}" | grep -q "$container"; then
            echo -e "$container: ${GREEN}✓ RUNNING${NC}"
            
            # Get container stats
            stats=$(docker stats "$container" --no-stream --format "table {{.CPUPerc}}\t{{.MemUsage}}" | tail -n 1)
            echo -e "  Stats: $stats"
        else
            echo -e "$container: ${RED}✗ NOT RUNNING${NC}"
        fi
    done
}

# Check system resources
check_resources() {
    echo -e "\n${BLUE}=== System Resources ===${NC}"
    
    # Disk usage
    echo "Disk Usage:"
    df -h "$PROJECT_ROOT" | tail -n 1 | awk '{printf "  Used: %s / Available: %s (Usage: %s)\n", $3, $4, $5}'
    
    # Memory usage
    if command -v free &> /dev/null; then
        echo "Memory Usage:"
        free -h | grep "Mem:" | awk '{printf "  Used: %s / Total: %s\n", $3, $2}'
    fi
    
    # Docker disk usage
    echo "Docker Disk Usage:"
    docker system df --format "table {{.Type}}\t{{.Total}}\t{{.Reclaimable}}\t{{.Size}}"
}

# Show logs
show_logs() {
    local service=${1:-all}
    echo -e "\n${BLUE}=== Recent Logs ===${NC}"
    
    case $service in
        "ors")
            docker logs --tail=20 ors-app
            ;;
        "nominatim")
            docker logs --tail=20 nominatim
            ;;
        "tileserver")
            docker logs --tail=20 tileserver
            ;;
        "all")
            echo -e "\n${YELLOW}ORS Logs:${NC}"
            docker logs --tail=10 ors-app 2>/dev/null || echo "ORS container not found"
            echo -e "\n${YELLOW}Nominatim Logs:${NC}"
            docker logs --tail=10 nominatim 2>/dev/null || echo "Nominatim container not found"
            echo -e "\n${YELLOW}Tileserver Logs:${NC}"
            docker logs --tail=10 tileserver 2>/dev/null || echo "Tileserver container not found"
            ;;
        *)
            echo "Usage: $0 {ors|nominatim|tileserver|all}"
            exit 1
            ;;
    esac
}

# Performance test
performance_test() {
    echo -e "\n${BLUE}=== Performance Test ===${NC}"
    
    # Test ORS routing performance
    echo "Testing ORS routing performance..."
    time_start=$(date +%s%3N)
    curl -s -X POST "http://localhost:$ORS_PORT/ors/v2/directions/driving-car" \
        -H "Content-Type: application/json" \
        -d '{"coordinates":[[14.5995,120.9842],[14.6760,121.0437],[15.1,120.7]]}' > /dev/null
    time_end=$(date +%s%3N)
    duration=$((time_end - time_start))
    echo "ORS routing took ${duration}ms"
    
    # Test Nominatim search performance
    echo "Testing Nominatim search performance..."
    time_start=$(date +%s%3N)
    curl -s "http://localhost:$NOMINATIM_PORT/search?q=Quezon City&format=json&limit=5" > /dev/null
    time_end=$(date +%s%3N)
    duration=$((time_end - time_start))
    echo "Nominatim search took ${duration}ms"
    
    # Test Tileserver tile performance
    echo "Testing Tileserver tile performance..."
    time_start=$(date +%s%3N)
    curl -s "http://localhost:$TILESERVER_PORT/styles/basic/10/500/500.png" > /dev/null
    time_end=$(date +%s%3N)
    duration=$((time_end - time_start))
    echo "Tileserver tile request took ${duration}ms"
}

# Main function
main() {
    load_environment
    
    echo -e "${BLUE}🔍 CCInfoMS Geoservices Health Check${NC}"
    echo -e "${BLUE}=====================================${NC}"
    
    case "${1:-check}" in
        "check")
            check_ors
            check_nominatim
            check_tileserver
            check_containers
            check_resources
            ;;
        "containers")
            check_containers
            ;;
        "resources")
            check_resources
            ;;
        "logs")
            show_logs "${2:-all}"
            ;;
        "performance")
            performance_test
            ;;
        "ors")
            check_ors
            ;;
        "nominatim")
            check_nominatim
            ;;
        "tileserver")
            check_tileserver
            ;;
        *)
            echo "Usage: $0 {check|containers|resources|logs|performance|ors|nominatim|tileserver}"
            echo ""
            echo "Commands:"
            echo "  check       - Full health check (default)"
            echo "  containers  - Check Docker containers"
            echo "  resources   - Check system resources"
            echo "  logs [service] - Show recent logs (all|ors|nominatim|tileserver)"
            echo "  performance - Run performance tests"
            echo "  ors         - Check ORS service only"
            echo "  nominatim   - Check Nominatim service only"
            echo "  tileserver  - Check Tileserver service only"
            exit 1
            ;;
    esac
}

main "$@"