# Build stage
FROM node:20-slim as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Serve stage
FROM node:20-slim
WORKDIR /app
RUN npm install -g http-server
COPY --from=builder /app/dist/employee-ui/browser /app
EXPOSE 4200
CMD ["http-server", "-p", "4200", "-c-1"]
