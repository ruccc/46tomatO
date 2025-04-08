//将身份转化为中文显示
export function parseRole(role: string | null) {
    if (role === 'admin') {
        return "管理员"
    }
    else if (role === 'customer') {
        return "顾客"
    }
    else {
        return "未知身份"
    }
}

//将时间转化为日常方式
export function parseTime(time: string) {
    let times = time.split(/T|\./)
    return times[0] + " " + times[1]
}



