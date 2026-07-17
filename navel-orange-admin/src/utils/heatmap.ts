/**
 * 简易热力图（不依赖 leaflet.heat）
 * 直接用 Canvas 绘制，返回一个带有 addTo 方法的对象
 */

export function createHeatLayer(points: Array<[number, number, number]>, options?: any) {
  const L = (window as any).L
  if (!L) return null

  const opts = Object.assign({
    radius: 40, blur: 25,
    gradient: { 0.3: '#34D399', 0.5: '#FBBF24', 0.7: '#F87171', 1.0: '#DC2626' }
  }, options)

  const heatPoints = points.map(p => ({ lat: p[0], lng: p[1], intensity: p[2] }))

  let map: any = null
  let canvas: HTMLCanvasElement | null = null
  let ctx: CanvasRenderingContext2D | null = null

  function draw() {
    if (!ctx || !map) return
    const size = map.getSize()
    ctx.clearRect(0, 0, size.x, size.y)

    const r = opts.radius
    const b = opts.blur
    const cs = (r + b) * 2

    // 预创建渐变圆
    const cc = document.createElement('canvas')
    cc.width = cc.height = cs
    const cctx = cc.getContext('2d')!
    const grad = cctx.createRadialGradient(cs / 2, cs / 2, 0, cs / 2, cs / 2, r + b)
    grad.addColorStop(0, 'rgba(0,0,0,0.6)')
    grad.addColorStop(0.5, 'rgba(0,0,0,0.3)')
    grad.addColorStop(1, 'rgba(0,0,0,0)')
    cctx.fillStyle = grad
    cctx.fillRect(0, 0, cs, cs)

    // 绘制热力点
    for (const p of heatPoints) {
      const px = map.latLngToContainerPoint([p.lat, p.lng])
      ctx.globalAlpha = Math.min((p.intensity || 0.5), 1) * 1.0
      ctx.drawImage(cc, px.x - cs / 2, px.y - cs / 2)
    }
    ctx.globalAlpha = 1

    // 着色
    const imgData = ctx.getImageData(0, 0, size.x, size.y)
    const data = imgData.data
    const colors = buildColors(opts.gradient)
    for (let i = 3; i < data.length; i += 4) {
      if (data[i] === 0) continue
      const idx = Math.min(Math.floor((data[i] / 255) * 255), 254)
      data[i - 3] = colors[idx].r
      data[i - 2] = colors[idx].g
      data[i - 1] = colors[idx].b
    }
    ctx.putImageData(imgData, 0, 0)
  }

  function buildColors(grad: Record<string, string>) {
    const stops = Object.entries(grad)
      .map(([k, v]) => ({ pos: parseFloat(k), ...hexToRgb(v) }))
      .sort((a, b) => a.pos - b.pos)
    const table: Array<{ r: number; g: number; b: number }> = []
    for (let i = 0; i < 256; i++) {
      const t = i / 255
      if (t <= stops[0].pos) { table.push(stops[0]); continue }
      if (t >= stops[stops.length - 1].pos) { table.push(stops[stops.length - 1]); continue }
      for (let j = 0; j < stops.length - 1; j++) {
        if (t >= stops[j].pos && t <= stops[j + 1].pos) {
          const p = (t - stops[j].pos) / (stops[j + 1].pos - stops[j].pos)
          table.push({
            r: Math.round(stops[j].r + (stops[j + 1].r - stops[j].r) * p),
            g: Math.round(stops[j].g + (stops[j + 1].g - stops[j].g) * p),
            b: Math.round(stops[j].b + (stops[j + 1].b - stops[j].b) * p)
          })
          break
        }
      }
    }
    return table
  }

  function hexToRgb(hex: string) {
    const m = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
    return m ? { r: parseInt(m[1], 16), g: parseInt(m[2], 16), b: parseInt(m[3], 16) }
      : { r: 0, g: 0, b: 0 }
  }

  function onAdd(m: any) {
    map = m
    canvas = L.DomUtil.create('canvas', 'leaflet-heatmap-layer')
    ctx = canvas.getContext('2d')
    const size = map.getSize()
    canvas!.width = size.x
    canvas!.height = size.y
    canvas!.style.cssText = 'position:absolute;left:0;top:0;pointer-events:none'
    map.getPanes().overlayPane.appendChild(canvas!)
    map.on('moveend', onMoveEnd)
    map.on('resize', onResize)
    draw()
  }

  function onRemove() {
    if (map) {
      map.off('moveend', onMoveEnd)
      map.off('resize', onResize)
    }
    if (canvas && canvas.parentNode) canvas.parentNode.removeChild(canvas)
    canvas = null; ctx = null; map = null
  }

  function onMoveEnd() {
    if (map && canvas) {
      L.DomUtil.setPosition(canvas, map.containerPointToLayerPoint([0, 0]))
      draw()
    }
  }

  function onResize() {
    if (!map || !canvas) return
    const size = map.getSize()
    canvas.width = size.x
    canvas.height = size.y
    draw()
  }

  return {
    addTo(m: any) { onAdd(m); return this },
    remove() { onRemove() },
    redraw() { draw() },
    setLatLngs(pts: Array<[number, number, number]>) {
      heatPoints.length = 0
      heatPoints.push(...pts.map(p => ({ lat: p[0], lng: p[1], intensity: p[2] })))
      draw()
    }
  }
}
