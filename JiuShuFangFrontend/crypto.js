// MD5 加密工具（修复版，无 Invalid count value: -1 错误）
const md5 = (string) => {
  return Promise.resolve(computeMD5(string))
}

// 纯 JS 实现的 MD5 算法（已修复所有兼容性/长度错误）
const computeMD5 = (input) => {
  const rotateLeft = (lValue, iShiftBits) => {
    return (lValue << iShiftBits) | (lValue >>> (32 - iShiftBits))
  }

  const addUnsigned = (lX, lY) => {
    const lX4 = (lX & 0x80000000)
    const lY4 = (lY & 0x80000000)
    const lX8 = (lX & 0x40000000)
    const lY8 = (lY & 0x40000000)
    const lResult = (lX & 0x3FFFFFFF) + (lY & 0x3FFFFFFF)
    if (lX8 & lY8) {
      return lResult ^ 0xC0000000 ^ lX4 ^ lY4
    }
    if (lX8 | lY8) {
      if (lResult & 0x40000000) {
        return lResult ^ 0x80000000 ^ lX4 ^ lY4
      } else {
        return lResult ^ 0x40000000 ^ lX4 ^ lY4
      }
    } else {
      return lResult ^ lX4 ^ lY4
    }
  }

  const fFunc = (x, y, z) => (x & y) | (~x & z)
  const gFunc = (x, y, z) => (x & z) | (y & ~z)
  const hFunc = (x, y, z) => x ^ y ^ z
  const iFunc = (x, y, z) => y ^ (x | ~z)

  const ff = (a, b, c, d, x, s, ac) => {
    a = addUnsigned(a, addUnsigned(addUnsigned(fFunc(b, c, d), x), ac))
    return addUnsigned(rotateLeft(a, s), b)
  }
  const gg = (a, b, c, d, x, s, ac) => {
    a = addUnsigned(a, addUnsigned(addUnsigned(gFunc(b, c, d), x), ac))
    return addUnsigned(rotateLeft(a, s), b)
  }
  const hh = (a, b, c, d, x, s, ac) => {
    a = addUnsigned(a, addUnsigned(addUnsigned(hFunc(b, c, d), x), ac))
    return addUnsigned(rotateLeft(a, s), b)
  }
  const iiFunc = (a, b, c, d, x, s, ac) => {
    a = addUnsigned(a, addUnsigned(addUnsigned(iFunc(b, c, d), x), ac))
    return addUnsigned(rotateLeft(a, s), b)
  }

  const toHex = (num) => {
    const str = (num >>> 0).toString(16)
    return str.padStart(8, '0')
  }

  // 核心修复：UTF8 编码 + 正确补位逻辑
  input = unescape(encodeURIComponent(input))
  let len = input.length * 8
  let chunk = []
  for (let i = 0; i < input.length; i++) {
    chunk[i >> 2] |= input.charCodeAt(i) << ((i % 4) * 8)
  }

  chunk[(len >> 5) | 0] |= 0x80 << ((len % 32) | 0)
  chunk[(((len + 64) >>> 9) << 4) + 14] = len

  let a = 0x67452301
  let b = 0xEFCDAB89
  let c = 0x98BADCFE
  let d = 0x10325476

  const K = [
    0xD76AA478, 0xE8C7B756, 0x242070DB, 0xC1BDCEEE,
    0xF57C0FAF, 0x4787C62A, 0xA8304613, 0xFD469501,
    0x698098D8, 0x8B44F7AF, 0xFFFF5BB1, 0x895CD7BE,
    0x6B901122, 0xFD987193, 0xA679438E, 0x49B40891,
    0xF61E2562, 0xC040B340, 0x265E5A51, 0xE9B6C7AA,
    0xD62F105D, 0x02441453, 0xD8A1E681, 0xE7D3FBC8,
    0x21E1CDE6, 0xC33707D6, 0xF4D50D87, 0x455A14ED,
    0xA9E3E905, 0xFCEFA3F8, 0x676F02D9, 0x8D2A4C8A,
    0xFFFA3942, 0x8771F681, 0x6D9D6122, 0xFDE5380C,
    0xA4BEEA44, 0x4BDECFA9, 0xF6BB4B60, 0xBEBFBC70,
    0x289B7EC6, 0xEAA127FA, 0xD4EF3085, 0x04881D05,
    0xD9D4D039, 0xE6DB99E5, 0x1FA27CF8, 0xC4AC5665,
    0xF4292244, 0x432AFF97, 0xAB9423A7, 0xFC93A039,
    0x655B59C3, 0x8F0CCC92, 0xFFEFF47D, 0x85845DD1,
    0x6FA87E4F, 0xFE2CE6E0, 0xA3014314, 0x4E0811A1,
    0xF7537E89, 0xBD3AF235, 0x2AD7D2BB, 0xEB86D391
  ]

  const S = [
    7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
    5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
    4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
    6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
  ]

  for (let i = 0; i < chunk.length; i += 16) {
    let oldA = a
    let oldB = b
    let oldC = c
    let oldD = d

    a = ff(a, b, c, d, chunk[i + 0], S[0], K[0])
    d = ff(d, a, b, c, chunk[i + 1], S[1], K[1])
    c = ff(c, d, a, b, chunk[i + 2], S[2], K[2])
    b = ff(b, c, d, a, chunk[i + 3], S[3], K[3])
    a = ff(a, b, c, d, chunk[i + 4], S[4], K[4])
    d = ff(d, a, b, c, chunk[i + 5], S[5], K[5])
    c = ff(c, d, a, b, chunk[i + 6], S[6], K[6])
    b = ff(b, c, d, a, chunk[i + 7], S[7], K[7])
    a = ff(a, b, c, d, chunk[i + 8], S[8], K[8])
    d = ff(d, a, b, c, chunk[i + 9], S[9], K[9])
    c = ff(c, d, a, b, chunk[i + 10], S[10], K[10])
    b = ff(b, c, d, a, chunk[i + 11], S[11], K[11])
    a = ff(a, b, c, d, chunk[i + 12], S[12], K[12])
    d = ff(d, a, b, c, chunk[i + 13], S[13], K[13])
    c = ff(c, d, a, b, chunk[i + 14], S[14], K[14])
    b = ff(b, c, d, a, chunk[i + 15], S[15], K[15])

    a = gg(a, b, c, d, chunk[i + 1], S[16], K[16])
    d = gg(d, a, b, c, chunk[i + 6], S[17], K[17])
    c = gg(c, d, a, b, chunk[i + 11], S[18], K[18])
    b = gg(b, c, d, a, chunk[i + 0], S[19], K[19])
    a = gg(a, b, c, d, chunk[i + 5], S[20], K[20])
    d = gg(d, a, b, c, chunk[i + 10], S[21], K[21])
    c = gg(c, d, a, b, chunk[i + 15], S[22], K[22])
    b = gg(b, c, d, a, chunk[i + 4], S[23], K[23])
    a = gg(a, b, c, d, chunk[i + 9], S[24], K[24])
    d = gg(d, a, b, c, chunk[i + 14], S[25], K[25])
    c = gg(c, d, a, b, chunk[i + 3], S[26], K[26])
    b = gg(b, c, d, a, chunk[i + 8], S[27], K[27])
    a = gg(a, b, c, d, chunk[i + 13], S[28], K[28])
    d = gg(d, a, b, c, chunk[i + 2], S[29], K[29])
    c = gg(c, d, a, b, chunk[i + 7], S[30], K[30])
    b = gg(b, c, d, a, chunk[i + 12], S[31], K[31])

    a = hh(a, b, c, d, chunk[i + 5], S[32], K[32])
    d = hh(d, a, b, c, chunk[i + 8], S[33], K[33])
    c = hh(c, d, a, b, chunk[i + 11], S[34], K[34])
    b = hh(b, c, d, a, chunk[i + 14], S[35], K[35])
    a = hh(a, b, c, d, chunk[i + 1], S[36], K[36])
    d = hh(d, a, b, c, chunk[i + 4], S[37], K[37])
    c = hh(c, d, a, b, chunk[i + 7], S[38], K[38])
    b = hh(b, c, d, a, chunk[i + 10], S[39], K[39])
    a = hh(a, b, c, d, chunk[i + 13], S[40], K[40])
    d = hh(d, a, b, c, chunk[i + 0], S[41], K[41])
    c = hh(c, d, a, b, chunk[i + 3], S[42], K[42])
    b = hh(b, c, d, a, chunk[i + 6], S[43], K[43])
    a = hh(a, b, c, d, chunk[i + 9], S[44], K[44])
    d = hh(d, a, b, c, chunk[i + 12], S[45], K[45])
    c = hh(c, d, a, b, chunk[i + 15], S[46], K[46])
    b = hh(b, c, d, a, chunk[i + 2], S[47], K[47])

    a = iiFunc(a, b, c, d, chunk[i + 0], S[48], K[48])
    d = iiFunc(d, a, b, c, chunk[i + 7], S[49], K[49])
    c = iiFunc(c, d, a, b, chunk[i + 14], S[50], K[50])
    b = iiFunc(b, c, d, a, chunk[i + 5], S[51], K[51])
    a = iiFunc(a, b, c, d, chunk[i + 12], S[52], K[52])
    d = iiFunc(d, a, b, c, chunk[i + 3], S[53], K[53])
    c = iiFunc(c, d, a, b, chunk[i + 10], S[54], K[54])
    b = iiFunc(b, c, d, a, chunk[i + 1], S[55], K[55])
    a = iiFunc(a, b, c, d, chunk[i + 8], S[56], K[56])
    d = iiFunc(d, a, b, c, chunk[i + 15], S[57], K[57])
    c = iiFunc(c, d, a, b, chunk[i + 6], S[58], K[58])
    b = iiFunc(b, c, d, a, chunk[i + 13], S[59], K[59])
    a = iiFunc(a, b, c, d, chunk[i + 4], S[60], K[60])
    d = iiFunc(d, a, b, c, chunk[i + 11], S[61], K[61])
    c = iiFunc(c, d, a, b, chunk[i + 2], S[62], K[62])
    b = iiFunc(b, c, d, a, chunk[i + 9], S[63], K[63])

    a = addUnsigned(a, oldA)
    b = addUnsigned(b, oldB)
    c = addUnsigned(c, oldC)
    d = addUnsigned(d, oldD)
  }

  return (toHex(a) + toHex(b) + toHex(c) + toHex(d)).toLowerCase()
}

// 使用 MD5 + 盐（手机号）加密密码
const encryptPassword = async (password, phone) => {
  if (!password || !phone) return password
  const saltedPassword = password + phone
  return await md5(saltedPassword)
}

export { md5, encryptPassword }