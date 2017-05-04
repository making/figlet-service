# figlet-service

```
  _____   ___    ____   _          _   
 |  ___| |_ _|  / ___| | |   ___  | |_ 
 | |_     | |  | |  _  | |  / _ \ | __|
 |  _|    | |  | |_| | | | |  __/ | |_ 
 |_|     |___|  \____| |_|  \___|  \__|
                                                      
```

[FIGlet](https://en.wikipedia.org/wiki/FIGlet) as a Cloud Foundry Route Service

## Deploy to Pivotal Web Services

```
./mvnw clean package -DskipTests=true
cf push
```

## Create Route Service

```
cf create-user-provided-service figlet-service -r https://figlet-service.cfapps.io
```

## Bind Route Service

```
cf bind-route-service cfapps.io figlet-service --hostname <your-subdomain>
```