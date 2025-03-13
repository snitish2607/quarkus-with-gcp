import http from 'k6/http'
import { sleep } from 'k6'

export const options = {
    stages: [
        { duration: '10s', target: 500 },
        { duration: '10s', target: 1000 },
    ]
}


export default function () {
    const url = 'https://randomdomain.store/auth'

    const payload = JSON.stringify({
        username: "nirp-admin",
        password: "password",
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    if (res.status !== 200) {
        console.error("Error: Received status code ${res.status}")
    }

    sleep(1)
}