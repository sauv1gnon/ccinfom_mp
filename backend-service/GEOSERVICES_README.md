# CCInfoMS Backend Geoservices Setup

Production-ready geospatial services infrastructure for the CCInfoMS healthcare management system.

## 🗺️ Overview

This setup provides a complete geoservices stack optimized for healthcare application needs in the Philippines:

- **ORS (OpenRouteService)**: High-performance routing and directions
- **Nominatim**: Geocoding and reverse geocoding for address resolution
- **Tileserver**: Map tile serving for visualization

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose installed
- At least 8GB RAM available
- Philippines OSM data (philippines.osm.pbf)
- Philippines MBTiles file (philippines.mbtiles)

### Automated Setup

```bash
# Navigate to backend service directory
cd backend-service

# Run complete setup
./scripts/setup.sh setup

# Check service health
./scripts/health-check.sh
```

### Manual Setup

```bash
# 1. Configure environment
cp .env.example .env  # If you don't have .env yet
# Edit .env with your preferences

# 2. Start services
docker-compose up -d

# 3. Monitor startup
docker-compose logs -f
```

## 📋 Service Endpoints

Once running, services will be available at:

| Service | Port | Endpoint | Description |
|---------|------|----------|-------------|
| **ORS** | 8080 | `http://localhost:8080/ors/v2/` | Routing API |
| **Nominatim** | 8081 | `http://localhost:8081/` | Geocoding API |
| **Tileserver** | 8082 | `http://localhost:8082/` | Map tiles |

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CCInfoMS      │    │   ORS Service   │    │  Nominatim      │
│   Application   │────│  (Port 8080)    │    │  (Port 8081)    │
│                 │    │                 │    │                 │
│ - Routing       │    │ - Directions    │    │ - Geocoding     │
│ - Map Display   │    │ - Isochrones    │    │ - Reverse Geo   │
│ - Location      │    │ - Matrix API    │    │ - Address Search│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                       ┌─────────────────┐    ┌─────────────────┐
                       │  Tileserver     │    │   Map Data      │
                       │  (Port 8082)    │    │                 │
                       │                 │    │ - philippines   │
                       │ - Map Tiles     │    │   .osm.pbf      │
                       │ - Vector Tiles  │    │ - philippines   │
                       │ - Styles        │    │   .mbtiles      │
                       └─────────────────┘    └─────────────────┘
```

## ⚙️ Configuration

### Environment Variables

Key configuration options in `.env`:

```bash
# Service Ports
ORS_HTTP_PORT=8080
NOMINATIM_HTTP_PORT=8081
TILESERVER_HTTP_PORT=8082

# ORS Performance Settings
ORS_XMS=4g              # Initial heap size
ORS_XMX=8g              # Maximum heap size
ORS_JAVA_OPTS=-XX:+UseG1GC -XX:+UseStringDeduplication

# Nominatim Settings
NOMINATIM_PASSWORD=ccinfom_prod_password_2024
NOMINATIM_THREADS=8
NOMINATIM_REPLICATION_URL=https://download.geofabrik.de/asia/philippines-updates/

# Tileserver Settings
TILESERVER_CACHE_SIZE=512MB
TILESERVER_MAX_THREADS=8
```

### ORS Configuration

The ORS service uses `/ors/ors-config.yml` with:
- **Version 9.3+** compatible schema
- Production-ready memory settings
- Optimized routing algorithms (CH + LM)
- Philippines-specific road priorities

### Nominatim Configuration

Uses `/nominatim/local.php` with:
- **Philippines-optimized** search settings
- Production CORS configuration
- Enhanced security headers
- Performance-tuned database settings

### Tileserver Configuration

Configured in `/tileserver/config/config.json` with:
- Multiple map styles (basic, satellite-hybrid)
- Philippines bounding box optimization
- Production caching and compression
- CORS-enabled for web applications

## 📊 Performance Tuning

### For Production

```bash
# Increase memory for large datasets
ORS_XMX=12g
NOMINATIM_THREADS=16

# Enable parallel processing
TILESERVER_MAX_THREADS=16

# Optimize for your use case
# - More RAM for faster routing
# - More CPU threads for faster imports
# - SSD storage for better I/O
```

### For Development

```bash
# Conservative resource usage
ORS_XMX=4g
NOMINATIM_THREADS=4
TILESERVER_MAX_THREADS=4
```

## 🔧 Management Commands

### Setup Scripts

```bash
# Complete setup
./scripts/setup.sh setup

# Start services
./scripts/setup.sh start

# Stop services
./scripts/setup.sh stop

# Restart services
./scripts/setup.sh restart

# Check service status
./scripts/setup.sh status

# View logs
./scripts/setup.sh logs

# Clean up
./scripts/setup.sh clean
```

### Health Monitoring

```bash
# Full health check
./scripts/health-check.sh check

# Check specific service
./scripts/health-check.sh ors
./scripts/health-check.sh nominatim
./scripts/health-check.sh tileserver

# Check Docker containers
./scripts/health-check.sh containers

# Check system resources
./scripts/health-check.sh resources

# Performance testing
./scripts/health-check.sh performance
```

## 🚦 API Usage Examples

### ORS Routing

```bash
# Get directions
curl -X POST "http://localhost:8080/ors/v2/directions/driving-car" \
  -H "Content-Type: application/json" \
  -d '{
    "coordinates":[[14.5995,120.9842],[14.6760,121.0437]],
    "instructions": true
  }'

# Get isochrones
curl -X POST "http://localhost:8080/ors/v2/isochrones/driving-car" \
  -H "Content-Type: application/json" \
  -d '{
    "coordinates":[[14.5995,120.9842]],
    "range":[300, 600, 900],
    "range_type":"time"
  }'
```

### Nominatim Geocoding

```bash
# Forward geocoding
curl "http://localhost:8081/search?q=Manila%20City%20Hall&format=json&limit=5"

# Reverse geocoding
curl "http://localhost:8081/reverse?lat=14.5995&lon=120.9842&format=json"

# With bounding box
curl "http://localhost:8081/search?q=hospital&format=json&viewbox=120.5,14.4,121.5,15.0&bounded=1"
```

### Tileserver

```bash
# Get tile
curl "http://localhost:8082/styles/basic/10/500/500.png"

# Get style info
curl "http://localhost:8082/styles/basic.json"

# Get vector tile
curl "http://localhost:8082/styles/basic/10/500/500.pbf"
```

## 📈 Monitoring

### Health Endpoints

- **ORS**: `GET /ors/v2/health`
- **Nominatim**: `GET /status.php`
- **Tileserver**: `GET /` (server info)

### Performance Metrics

Monitor these key metrics:

1. **ORS Response Time**: Should be < 2s for routing requests
2. **Nominatim Import Time**: Initial import may take 2-4 hours
3. **Tile Server Cache Hit Rate**: Target > 90%
4. **Memory Usage**: Monitor container memory consumption
5. **Disk Space**: Ensure sufficient space for graphs and caches

### Logging

Logs are available via:

```bash
# All service logs
docker-compose logs -f

# Specific service
docker-compose logs -f ors-app
docker-compose logs -f nominatim
docker-compose logs -f tileserver

# Last 50 lines
docker logs --tail=50 ors-app
```

## 🔧 Troubleshooting

### Common Issues

#### ORS Service Won't Start

```bash
# Check ORS configuration
docker logs ors-app | grep ERROR

# Verify graph files exist
ls -la ors/graphs/

# Rebuild graphs if needed
export ORS_REBUILD_GRAPHS=true
docker-compose up -d ors-app
```

#### Nominatim Import Slow

```bash
# Check import progress
docker logs nominatim | grep -i "processed\|imported"

# Increase threads (temporarily)
export NOMINATIM_THREADS=16
docker-compose up -d nominatim

# Monitor PostgreSQL
docker exec -it geo_services_module_nominatim_1 psql -U nominatim -d nominatim -c "SELECT pg_size_pretty(pg_database_size('nominatim'));"
```

#### Tileserver Tiles Not Loading

```bash
# Check MBTiles file
file data/philippines.mbtiles

# Verify tile server logs
docker logs tileserver

# Test tile endpoint
curl -I "http://localhost:8082/styles/basic/0/0/0.png"
```

#### Port Conflicts

If you encounter port conflicts:

```bash
# Check what's using the ports
netstat -tulpn | grep :808

# Update ports in .env
ORS_HTTP_PORT=8080
NOMINATIM_HTTP_PORT=8081  
TILESERVER_HTTP_PORT=8082
```

#### Memory Issues

```bash
# Monitor memory usage
docker stats

# Increase memory limits in docker-compose.yaml
# or reduce resource usage in .env

# Clean up Docker resources
docker system prune -f
```

### Reset and Rebuild

If you need to start fresh:

```bash
# Stop and remove all data
./scripts/setup.sh clean

# Remove volumes (WARNING: This deletes all data)
docker-compose down -v

# Start fresh
./scripts/setup.sh setup
```

## 🔄 Data Updates

### Update Philippines OSM Data

```bash
# Download latest Philippines data
wget https://download.geofabrik.de/asia/philippines-latest.osm.pbf -O data/philippines_new.osm.pbf

# Replace old file
mv data/philippines_new.osm.pbf data/philippines.osm.pbf

# Rebuild Nominatim
export ORS_REBUILD_GRAPHS=true
export NOMINATIM_REBUILD=true
docker-compose up -d
```

### Update Nominatim Replication

```bash
# Enable auto-updates
export NOMINATIM_REPLICATION_URL=https://download.geofabrik.de/asia/philippines-updates/

# Restart Nominatim
docker-compose restart nominatim
```

## 🏥 Healthcare-Specific Features

### Optimized for Medical Applications

1. **Emergency Routing**: High-priority routes for ambulance services
2. **Hospital Search**: Enhanced search for medical facilities
3. **Philippines Coverage**: Bounding box optimized for Philippines
4. **CORS Enabled**: Ready for web application integration
5. **Production Ready**: Comprehensive error handling and monitoring

### Integration Points

- **Patient Registration**: Use Nominatim for address validation
- **Appointment Scheduling**: Use ORS for travel time estimates
- **Emergency Services**: Use ORS for fastest route calculations
- **Location Display**: Use Tileserver for map visualization

## 📚 Additional Resources

### Documentation Links

- [OpenRouteService Documentation](https://giscience.github.io/openrouteservice/)
- [Nominatim Documentation](https://nominatim.org/release-docs/latest/)
- [TileServer GL Documentation](https://tileserver.readthedocs.io/)

### Data Sources

- [OpenStreetMap Philippines](https://download.geofabrik.de/asia/philippines.html)
- [Philippines MBTiles](Generated from OSM data)

## 🆘 Support

For issues and support:

1. Check the logs: `./scripts/health-check.sh logs`
2. Review this documentation
3. Check service-specific documentation
4. Open an issue with log details and configuration

## 📝 License

- **ORS**: BSD 2-Clause License
- **Nominatim**: PostgreSQL License  
- **Tileserver GL**: BSD 2-Clause License
- **Map Data**: OpenStreetMap Contributors License

---

**Ready for bootstrapping and initialization!** 🎉

The complete CCInfoMS geoservices stack is now configured for production use with Philippines-optimized settings, comprehensive monitoring, and healthcare application integration.