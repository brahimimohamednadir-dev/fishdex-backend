#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# FishDex Backend — Script de setup Mac (première installation)
# Usage : chmod +x setup.sh && ./setup.sh
# ─────────────────────────────────────────────────────────────────────────────

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()    { echo -e "${CYAN}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[OK]${NC}   $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
error()   { echo -e "${RED}[ERR]${NC}  $1"; exit 1; }

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║       FishDex Backend — Setup Mac        ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# ── 1. Homebrew ───────────────────────────────────────────────────────────────
if ! command -v brew &>/dev/null; then
  info "Installation de Homebrew..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  success "Homebrew installé"
else
  success "Homebrew déjà présent"
fi

# ── 2. Java 21 ───────────────────────────────────────────────────────────────
if ! java -version 2>&1 | grep -q "21"; then
  info "Installation de Java 21..."
  brew install --cask temurin@21
  success "Java 21 installé"
else
  success "Java 21 déjà présent"
fi

# ── 3. Maven ─────────────────────────────────────────────────────────────────
if ! command -v mvn &>/dev/null; then
  info "Installation de Maven..."
  brew install maven
  success "Maven installé"
else
  success "Maven $(mvn -q --version 2>&1 | head -1) déjà présent"
fi

# ── 4. MySQL 8 ───────────────────────────────────────────────────────────────
if ! command -v mysql &>/dev/null; then
  info "Installation de MySQL 8..."
  brew install mysql@8.0
  brew link mysql@8.0 --force
  brew services start mysql@8.0
  sleep 3
  success "MySQL 8 installé et démarré"
else
  info "MySQL déjà présent — démarrage du service..."
  brew services start mysql 2>/dev/null || brew services start mysql@8.0 2>/dev/null || true
  success "MySQL démarré"
fi

# ── 5. Création de la base de données ────────────────────────────────────────
echo ""
info "Création de la base de données fishdex..."

read -rsp "Mot de passe MySQL root (laisse vide si pas de mot de passe) : " DB_PASS
echo ""

if [ -z "$DB_PASS" ]; then
  MYSQL_CMD="mysql -u root"
else
  MYSQL_CMD="mysql -u root -p$DB_PASS"
fi

$MYSQL_CMD -e "CREATE DATABASE IF NOT EXISTS fishdex CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" \
  && success "Base de données 'fishdex' prête" \
  || error "Impossible de créer la base. Vérifie que MySQL tourne et que le mot de passe est correct."

# ── 6. Fichier .env local ────────────────────────────────────────────────────
echo ""
info "Génération du fichier .env..."

JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')

cat > .env <<EOF
# ── Base de données ──────────────────────────────────────────────────────────
DB_URL=jdbc:mysql://localhost:3306/fishdex
DB_USERNAME=root
DB_PASSWORD=${DB_PASS}

# ── JWT ──────────────────────────────────────────────────────────────────────
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# ── App ──────────────────────────────────────────────────────────────────────
APP_BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:4200
APP_DEV_DATA=true
DDL_AUTO=update

# ── Email (optionnel en dev) ─────────────────────────────────────────────────
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=noreply@fishdex.fr

# ── Cloudinary (optionnel en dev — fallback stockage local) ──────────────────
CLOUDINARY_CLOUD_NAME=demo
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
EOF

success ".env généré avec un JWT_SECRET aléatoire"

# ── 7. Export des variables pour le lancement ─────────────────────────────────
set -a
source .env
set +a

# ── 8. Build Maven ────────────────────────────────────────────────────────────
echo ""
info "Build Maven (première compilation, ~1 min)..."
mvn clean package "-Dmaven.test.skip=true" -q \
  && success "Build réussi" \
  || error "Échec du build Maven. Lance 'mvn clean package -Dmaven.test.skip=true' pour voir les erreurs."

# ── 9. Lancement ─────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Setup terminé ! Démarrage du backend FishDex...     ║${NC}"
echo -e "${GREEN}║  API dispo sur : http://localhost:8080/api            ║${NC}"
echo -e "${GREEN}║  Ctrl+C pour arrêter                                  ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
echo ""
info "Les tables MySQL et les données (espèces) seront créées automatiquement."
echo ""

mvn spring-boot:run "-Dmaven.test.skip=true"
