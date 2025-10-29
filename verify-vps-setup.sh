#!/bin/bash

# Script kiểm tra VPS đã setup đầy đủ chưa
# Chạy: bash verify-vps-setup.sh

echo "=================================="
echo "VPS Setup Verification Script"
echo "=================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check function
check_command() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 is installed"
        if [ ! -z "$2" ]; then
            echo "  Version: $($1 $2 2>&1 | head -1)"
        fi
        return 0
    else
        echo -e "${RED}✗${NC} $1 is NOT installed"
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} File exists: $1"
        return 0
    else
        echo -e "${RED}✗${NC} File NOT found: $1"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} Directory exists: $1"
        return 0
    else
        echo -e "${RED}✗${NC} Directory NOT found: $1"
        return 1
    fi
}

# Counter
TOTAL=0
PASSED=0

echo "1. Checking System Requirements..."
echo "-----------------------------------"

# Docker
check_command docker "--version"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# Docker Compose
check_command docker "compose version"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# curl
check_command curl "--version"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

echo ""
echo "2. Checking Project Directory..."
echo "-----------------------------------"

# Project directory
check_dir "/opt/pbl6-backend"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# docker-compose.yml
check_file "/opt/pbl6-backend/docker-compose.yml"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# .env file
check_file "/opt/pbl6-backend/.env"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

echo ""
echo "3. Checking SSH Keys..."
echo "-----------------------------------"

# SSH directory
check_dir "$HOME/.ssh"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# GitHub Actions key
check_file "$HOME/.ssh/github_actions_key"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

check_file "$HOME/.ssh/github_actions_key.pub"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

# authorized_keys
check_file "$HOME/.ssh/authorized_keys"
if [ $? -eq 0 ]; then ((PASSED++)); fi
((TOTAL++))

echo ""
echo "4. Checking Docker Login..."
echo "-----------------------------------"

# Check Docker config
if [ -f "$HOME/.docker/config.json" ]; then
    if grep -q "ghcr.io" "$HOME/.docker/config.json"; then
        echo -e "${GREEN}✓${NC} Logged in to GitHub Container Registry"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} NOT logged in to GitHub Container Registry"
    fi
else
    echo -e "${RED}✗${NC} Docker config not found"
fi
((TOTAL++))

echo ""
echo "5. Checking Firewall (UFW)..."
echo "-----------------------------------"

if command -v ufw &> /dev/null; then
    echo -e "${GREEN}✓${NC} UFW is installed"
    
    UFW_STATUS=$(sudo ufw status 2>/dev/null | grep -i "Status:" | awk '{print $2}')
    if [ "$UFW_STATUS" = "active" ]; then
        echo -e "${GREEN}✓${NC} UFW is active"
        ((PASSED++))
    else
        echo -e "${YELLOW}!${NC} UFW is not active"
    fi
    ((TOTAL++))
    
    # Check SSH port
    if sudo ufw status | grep -q "22/tcp"; then
        echo -e "${GREEN}✓${NC} SSH port (22) is allowed"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} SSH port (22) is NOT allowed (DANGEROUS!)"
    fi
    ((TOTAL++))
else
    echo -e "${YELLOW}!${NC} UFW is not installed"
    ((TOTAL++))
fi

echo ""
echo "6. Checking Environment Variables..."
echo "-----------------------------------"

if [ -f "/opt/pbl6-backend/.env" ]; then
    # Check required variables
    REQUIRED_VARS=(
        "REGISTRY_USERNAME"
        "AUTH_DB_PASSWORD"
        "JWT_SECRET"
        "GOONGMAP_API_KEY"
        "CORS_ALLOWED_ORIGINS"
    )
    
    for VAR in "${REQUIRED_VARS[@]}"; do
        if grep -q "^${VAR}=" "/opt/pbl6-backend/.env"; then
            VALUE=$(grep "^${VAR}=" "/opt/pbl6-backend/.env" | cut -d'=' -f2)
            if [[ "$VALUE" != *"YOUR_"* ]] && [[ "$VALUE" != *"your_"* ]] && [ ! -z "$VALUE" ]; then
                echo -e "${GREEN}✓${NC} $VAR is set"
                ((PASSED++))
            else
                echo -e "${RED}✗${NC} $VAR is NOT properly configured"
            fi
        else
            echo -e "${RED}✗${NC} $VAR is missing"
        fi
        ((TOTAL++))
    done
fi

echo ""
echo "7. Checking System Resources..."
echo "-----------------------------------"

# Disk space
DISK_AVAIL=$(df -h /opt | tail -1 | awk '{print $4}')
echo "  Available disk space: $DISK_AVAIL"

# Memory
MEM_TOTAL=$(free -h | grep Mem | awk '{print $2}')
MEM_AVAIL=$(free -h | grep Mem | awk '{print $7}')
echo "  Total memory: $MEM_TOTAL"
echo "  Available memory: $MEM_AVAIL"

echo ""
echo "8. Checking Docker Containers..."
echo "-----------------------------------"

if [ -d "/opt/pbl6-backend" ]; then
    cd /opt/pbl6-backend
    
    # Check if any containers are running
    RUNNING=$(docker compose ps --services --filter "status=running" 2>/dev/null | wc -l)
    if [ $RUNNING -gt 0 ]; then
        echo -e "${GREEN}✓${NC} $RUNNING container(s) are running"
        docker compose ps
    else
        echo -e "${YELLOW}!${NC} No containers are running (normal if not deployed yet)"
    fi
fi

echo ""
echo "=================================="
echo "SUMMARY"
echo "=================================="
echo "Passed: $PASSED / $TOTAL checks"
echo ""

if [ $PASSED -eq $TOTAL ]; then
    echo -e "${GREEN}✓ VPS is ready for deployment!${NC}"
    exit 0
elif [ $PASSED -ge $((TOTAL * 80 / 100)) ]; then
    echo -e "${YELLOW}⚠ VPS is mostly ready, but some issues need attention${NC}"
    exit 0
else
    echo -e "${RED}✗ VPS setup is incomplete. Please review the failed checks above.${NC}"
    exit 1
fi
