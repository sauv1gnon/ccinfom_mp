#!/bin/bash
# CCInfoMS Geoservices Setup Script
# Production-ready bootstrap script for backend geoservices

set -e

echo "🚀 Starting CCInfoMS Geoservices Setup..."
echo "========================================"

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"
DOCKER_COMPOSE_FILE="$PROJECT_ROOT/docker-compose.yaml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
    fi
    
    # Check environment file
    if [ ! -f "$ENV_FILE" ]; then
        error "Environment file (.env) not found. Please ensure it exists."
    fi
    
    success "All prerequisites met."
}

# Create required directories
setup_directories() {
    log "Creating required directories..."
    
    # ORS directories
    mkdir -p "$PROJECT_ROOT/ors/graphs"
    mkdir -p "$PROJECT_ROOT/ors/elevation_cache" 
    mkdir -p "$PROJECT_ROOT/ors/logs"
    
    # Nominatim directories
    mkdir -p "$PROJECT_ROOT/nominatim/data"
    mkdir -p "$PROJECT_ROOT/nominatim/flatnode"
    mkdir -p "$PROJECT_ROOT/nominatim/logs"
    
    # Tileserver directories
    mkdir -p "$PROJECT_ROOT/tileserver/logs"
    mkdir -p "$PROJECT_ROOT/tileserver/cache"
    
    # Data directories
    mkdir -p "$PROJECT_ROOT/data/backups"
    mkdir -p "$PROJECT_ROOT/data/exports"
    
    success "Directory structure created."
}

# Set permissions
setup_permissions() {
    log "Setting up permissions..."
    
    # Make scripts executable
    chmod +x "$PROJECT_ROOT/scripts"/*.sh 2>/dev/null || true
    
    # Set proper permissions for data directories
    chmod -R 755 "$PROJECT_ROOT/ors" 2>/dev/null || true
    chmod -R 755 "$PROJECT_ROOT/nominatim" 2>/dev/null || true
    chmod -R 755 "$PROJECT_ROOT/tileserver" 2>/dev/null || true
    
    success "Permissions configured."
}

# Load environment variables
load_environment() {
    log "Loading environment variables..."
    
    if [ -f "$ENV_FILE" ]; then
        export $(grep -v '^#' "$ENV_FILE" | xargs)
        success "Environment variables loaded."
    else
        error "Environment file not found at $ENV_FILE"
    fi
}

# Validate configuration
validate_config() {
    log "Validating configuration..."
    
    # Check required data files
    if [ ! -f "$PROJECT_ROOT/data/philippines.osm.pbf" ]; then
        warning "Philippines OSM data file not found. Please ensure philippiines.osm.pbf exists in data directory."
    fi
    
    if [ ! -f "$PROJECT_ROOT/data/philippines.mbtiles" ]; then
        warning "Philippines MBTiles file not found. Please ensure philippines.mbtiles exists in data directory."
    fi
    
    # Check configuration files
    if [ ! -f "$PROJECT_ROOT/ors/ors-config.yml" ]; then
        error "ORS configuration file not found."
    fi
    
    if [ ! -f "$PROJECT_ROOT/tileserver/config/config.json" ]; then
        error "Tileserver configuration file not found."
    fi
    
    if [ ! -f "$PROJECT_ROOT/nominatim/local.php" ]; then
        error "Nominatim configuration file not found."
    fi
    
    success "Configuration validation completed."
}

# Start services
start_services() {
    log "Starting geoservices..."
    
    # Pull latest images
    log "Pulling latest Docker images..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" pull
    
    # Start services in order
    log "Starting Nominatim service (this will take time for initial import)..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d nominatim
    
    log "Waiting for Nominatim to be ready..."
    timeout 300 bash -c 'until curl -f http://localhost:8081/status.php; do sleep 10; done' || warning "Nominatim health check failed"
    
    log "Starting ORS service..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d ors-app
    
    log "Waiting for ORS to be ready..."
    timeout 60 bash -c 'until curl -f http://localhost:8080/ors/v2/health; do sleep 5; done' || warning "ORS health check failed"
    
    log "Starting Tileserver service..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d tileserver
    
    log "Waiting for Tileserver to be ready..."
    timeout 60 bash -c 'until curl -f http://localhost:8082/; do sleep 5; done' || warning "Tileserver health check failed"
    
    success "All services started successfully!"
}

# Show service status
show_status() {
    log "Service Status:"
    echo "=============="
    
    # Check running containers
    docker-compose -f "$DOCKER_COMPOSE_FILE" ps
    
    # Test service endpoints
    log "\nService Endpoints:"
    echo "=================="
    echo "ORS (Routing):     http://localhost:${ORS_HTTP_PORT:-8080}/ors/v2/"
    echo "Nominatim:         http://localhost:${NOMINATIM_HTTP_PORT:-8081}/"
    echo "Tileserver:        http://localhost:${TILESERVER_HTTP_PORT:-8082}/"
    
    log "\nTesting service health..."
    
    # Test ORS
    if curl -f -s "http://localhost:${ORS_HTTP_PORT:-8080}/ors/v2/health" > /dev/null; then
        success "ORS is healthy"
    else
        warning "ORS health check failed"
    fi
    
    # Test Nominatim
    if curl -f -s "http://localhost:${NOMINATIM_HTTP_PORT:-8081}/status.php" > /dev/null; then
        success "Nominatim is healthy"
    else
        warning "Nominatim health check failed"
    fi
    
    # Test Tileserver
    if curl -f -s "http://localhost:${TILESERVER_HTTP_PORT:-8082}/" > /dev/null; then
        success "Tileserver is healthy"
    else
        warning "Tileserver health check failed"
    fi
}

# Show usage information
show_usage() {
    log "\nUsage Information:"
    echo "=================="
    echo "1. Start all services:     ./setup.sh start"
    echo "2. Stop all services:      ./setup.sh stop"
    echo "3. Restart services:       ./setup.sh restart"
    echo "4. View logs:              ./setup.sh logs"
    echo "5. Check status:           ./setup.sh status"
    echo "6. Clean up:               ./setup.sh clean"
    echo ""
    echo "Individual service management:"
    echo "7. Start only ORS:         ./setup.sh start-ors"
    echo "8. Start only Nominatim:   ./setup.sh start-nominatim"
    echo "9. Start only Tileserver:  ./setup.sh start-tileserver"
}

# Main execution
main() {
    case "${1:-setup}" in
        "setup")
            check_prerequisites
            load_environment
            setup_directories
            setup_permissions
            validate_config
            start_services
            show_status
            show_usage
            success "Setup completed successfully! 🎉"
            ;;
        "start")
            load_environment
            start_services
            show_status
            ;;
        "stop")
            log "Stopping all services..."
            docker-compose -f "$DOCKER_COMPOSE_FILE" stop
            success "All services stopped."
            ;;
        "restart")
            log "Restarting all services..."
            docker-compose -f "$DOCKER_COMPOSE_FILE" restart
            show_status
            ;;
        "status")
            show_status
            ;;
        "logs")
            log "Showing service logs..."
            docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f
            ;;
        "clean")
            log "Cleaning up containers and images..."
            docker-compose -f "$DOCKER_COMPOSE_FILE" down -v
            docker system prune -f
            success "Cleanup completed."
            ;;
        "start-ors")
            load_environment
            docker-compose -f "$DOCKER_COMPOSE_FILE" up -d ors-app
            ;;
        "start-nominatim")
            load_environment
            docker-compose -f "$DOCKER_COMPOSE_FILE" up -d nominatim
            ;;
        "start-tileserver")
            load_environment
            docker-compose -f "$DOCKER_COMPOSE_FILE" up -d tileserver
            ;;
        *)
            echo "Usage: $0 {setup|start|stop|restart|status|logs|clean|start-ors|start-nominatim|start-tileserver}"
            exit 1
            ;;
    esac
}

# Run main function
main "$@"