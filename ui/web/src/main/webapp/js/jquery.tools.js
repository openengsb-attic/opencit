/*
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function(c) {
    function p(d, a, b) {
        var e = this,l = d.add(this),h = d.find(b.tabs),j = a.jquery ? a : d.children(a),i;
        h.length || (h = d.children());
        j.length || (j = d.parent().find(a));
        j.length || (j = c(a));
        c.extend(this, {click:function(f, g) {
            var k = h.eq(f);
            if (typeof f == "string" && f.replace("#", "")) {
                k = h.filter("[href*=" + f.replace("#", "") + "]");
                f = Math.max(h.index(k), 0)
            }
            if (b.rotate) {
                var n = h.length - 1;
                if (f < 0)return e.click(n, g);
                if (f > n)return e.click(0, g)
            }
            if (!k.length) {
                if (i >= 0)return e;
                f = b.initialIndex;
                k = h.eq(f)
            }
            if (f === i)return e;
            g = g || c.Event();
            g.type = "onBeforeClick";
            l.trigger(g, [f]);
            if (!g.isDefaultPrevented()) {
                o[b.effect].call(e, f, function() {
                    g.type = "onClick";
                    l.trigger(g, [f])
                });
                i = f;
                h.removeClass(b.current);
                k.addClass(b.current);
                return e
            }
        },getConf:function() {
            return b
        },getTabs:function() {
            return h
        },getPanes:function() {
            return j
        },getCurrentPane:function() {
            return j.eq(i)
        },getCurrentTab:function() {
            return h.eq(i)
        },getIndex:function() {
            return i
        },next:function() {
            return e.click(i + 1)
        },prev:function() {
            return e.click(i - 1)
        }});
        c.each("onBeforeClick,onClick".split(","), function(f, g) {
            c.isFunction(b[g]) && c(e).bind(g, b[g]);
            e[g] = function(k) {
                c(e).bind(g, k);
                return e
            }
        });
        if (b.history && c.fn.history) {
            c.tools.history.init(h);
            b.event = "history"
        }
        h.each(function(f) {
            c(this).bind(b.event, function(g) {
                e.click(f, g);
                return g.preventDefault()
            })
        });
        j.find("a[href^=#]").click(function(f) {
            e.click(c(this).attr("href"), f)
        });
        if (location.hash)e.click(location.hash); else if (b.initialIndex === 0 || b.initialIndex
                > 0)e.click(b.initialIndex)
    }

    c.tools = c.tools || {version:"1.2.2"};
    c.tools.tabs = {conf:{tabs:"a",
        current:"current",onBeforeClick:null,onClick:null,effect:"default",initialIndex:0,event:"click",rotate:false,history:false},addEffect:function(
            d, a) {
        o[d] = a
    }};
    var o = {"default":function(d, a) {
        this.getPanes().hide().eq(d).show();
        a.call()
    },fade:function(d, a) {
        var b = this.getConf(),e = b.fadeOutSpeed,l = this.getPanes();
        e ? l.fadeOut(e) : l.hide();
        l.eq(d).fadeIn(b.fadeInSpeed, a)
    },slide:function(d, a) {
        this.getPanes().slideUp(200);
        this.getPanes().eq(d).slideDown(400, a)
    },ajax:function(d, a) {
        this.getPanes().eq(0).load(this.getTabs().eq(d).attr("href"), a)
    }},m;
    c.tools.tabs.addEffect("horizontal", function(d, a) {
        m || (m = this.getPanes().eq(0).width());
        this.getCurrentPane().animate({width:0}, function() {
            c(this).hide()
        });
        this.getPanes().eq(d).animate({width:m}, function() {
            c(this).show();
            a.call()
        })
    });
    c.fn.tabs = function(d, a) {
        var b = this.data("tabs");
        if (b)return b;
        if (c.isFunction(a))a = {onBeforeClick:a};
        a = c.extend({}, c.tools.tabs.conf, a);
        this.each(function() {
            b = new p(c(this), d, a);
            c(this).data("tabs", b)
        });
        return a.api ? b : this
    }
})(jQuery);
(function(d) {
    function r(g, a) {
        function p(f) {
            var e = d(f);
            return e.length < 2 ? e : g.parent().find(f)
        }

        var c = this,j = g.add(this),b = g.data("tabs"),h,l,m,n = false,o = p(a.next).click(function() {
            b.next()
        }),k = p(a.prev).click(function() {
            b.prev()
        });
        d.extend(c, {getTabs:function() {
            return b
        },getConf:function() {
            return a
        },play:function() {
            if (!h) {
                var f = d.Event("onBeforePlay");
                j.trigger(f);
                if (f.isDefaultPrevented())return c;
                n = false;
                h = setInterval(b.next, a.interval);
                j.trigger("onPlay");
                b.next()
            }
        },pause:function() {
            if (!h)return c;
            var f = d.Event("onBeforePause");
            j.trigger(f);
            if (f.isDefaultPrevented())return c;
            h = clearInterval(h);
            m = clearInterval(m);
            j.trigger("onPause")
        },stop:function() {
            c.pause();
            n = true
        }});
        d.each("onBeforePlay,onPlay,onBeforePause,onPause".split(","), function(f, e) {
            d.isFunction(a[e]) && c.bind(e, a[e]);
            c[e] = function(s) {
                return c.bind(e, s)
            }
        });
        if (a.autopause) {
            var t = b.getTabs().add(o).add(k).add(b.getPanes());
            t.hover(function() {
                c.pause();
                l = clearInterval(l)
            }, function() {
                n || (l = setTimeout(c.play, a.interval))
            })
        }
        if (a.autoplay)m = setTimeout(c.play, a.interval); else c.stop();
        a.clickable && b.getPanes().click(function() {
            b.next()
        });
        if (!b.getConf().rotate) {
            var i = a.disabledClass;
            b.getIndex() || k.addClass(i);
            b.onBeforeClick(function(f, e) {
                if (e) {
                    k.removeClass(i);
                    e == b.getTabs().length - 1 ? o.addClass(i) : o.removeClass(i)
                } else k.addClass(i)
            })
        }
    }

    var q;
    q = d.tools.tabs.slideshow
            = {conf:{next:".forward",prev:".backward",disabledClass:"disabled",autoplay:false,autopause:true,interval:3E3,clickable:true,api:false}};
    d.fn.slideshow = function(g) {
        var a = this.data("slideshow");
        if (a)return a;
        g = d.extend({}, q.conf, g);
        this.each(function() {
            a = new r(d(this), g);
            d(this).data("slideshow", a)
        });
        return g.api ? a : this
    }
})(jQuery);
(function(f) {
    function p(a, b, c) {
        var h = c.relative ? a.position().top : a.offset().top,e = c.relative ? a.position().left
                : a.offset().left,i = c.position[0];
        h -= b.outerHeight() - c.offset[0];
        e += a.outerWidth() + c.offset[1];
        var j = b.outerHeight() + a.outerHeight();
        if (i == "center")h += j / 2;
        if (i == "bottom")h += j;
        i = c.position[1];
        a = b.outerWidth() + a.outerWidth();
        if (i == "center")e -= a / 2;
        if (i == "left")e -= a;
        return{top:h,left:e}
    }

    function t(a, b) {
        var c = this,h = a.add(c),e,i = 0,j = 0,m = a.attr("title"),q = n[b.effect],k,r = a.is(":input"),u = r
                && a.is(":checkbox, :radio, select, :button"),
                s = a.attr("type"),l = b.events[s] || b.events[r ? u ? "widget" : "input" : "def"];
        if (!q)throw'Nonexistent effect "' + b.effect + '"';
        l = l.split(/,\s*/);
        if (l.length != 2)throw"Tooltip: bad events configuration for " + s;
        a.bind(l[0], function(d) {
            if (b.predelay) {
                clearTimeout(i);
                j = setTimeout(function() {
                    c.show(d)
                }, b.predelay)
            } else c.show(d)
        }).bind(l[1], function(d) {
            if (b.delay) {
                clearTimeout(j);
                i = setTimeout(function() {
                    c.hide(d)
                }, b.delay)
            } else c.hide(d)
        });
        if (m && b.cancelDefault) {
            a.removeAttr("title");
            a.data("title", m)
        }
        f.extend(c, {show:function(d) {
            if (!e) {
                if (m)e = f(b.layout).addClass(b.tipClass).appendTo(document.body).hide().append(m); else if (b.tip)e
                        = f(b.tip).eq(0); else {
                    e = a.next();
                    e.length || (e = a.parent().next())
                }
                if (!e.length)throw"Cannot find tooltip for " + a;
            }
            if (c.isShown())return c;
            e.stop(true, true);
            var g = p(a, e, b);
            d = d || f.Event();
            d.type = "onBeforeShow";
            h.trigger(d, [g]);
            if (d.isDefaultPrevented())return c;
            g = p(a, e, b);
            e.css({position:"absolute",top:g.top,left:g.left});
            k = true;
            q[0].call(c, function() {
                d.type = "onShow";
                k = "full";
                h.trigger(d)
            });
            g = b.events.tooltip.split(/,\s*/);
            e.bind(g[0], function() {
                clearTimeout(i);
                clearTimeout(j)
            });
            g[1] && !a.is("input:not(:checkbox, :radio), textarea") && e.bind(g[1], function(o) {
                o.relatedTarget != a[0] && a.trigger(l[1].split(" ")[0])
            });
            return c
        },hide:function(d) {
            if (!e || !c.isShown())return c;
            d = d || f.Event();
            d.type = "onBeforeHide";
            h.trigger(d);
            if (!d.isDefaultPrevented()) {
                k = false;
                n[b.effect][1].call(c, function() {
                    d.type = "onHide";
                    k = false;
                    h.trigger(d)
                });
                return c
            }
        },isShown:function(d) {
            return d ? k == "full" : k
        },getConf:function() {
            return b
        },
            getTip:function() {
                return e
            },getTrigger:function() {
                return a
            }});
        f.each("onHide,onBeforeShow,onShow,onBeforeHide".split(","), function(d, g) {
            f.isFunction(b[g]) && f(c).bind(g, b[g]);
            c[g] = function(o) {
                f(c).bind(g, o);
                return c
            }
        })
    }

    f.tools = f.tools || {version:"1.2.2"};
    f.tools.tooltip
            = {conf:{effect:"toggle",fadeOutSpeed:"fast",predelay:0,delay:30,opacity:1,tip:0,position:["top","center"],offset:[0,0],relative:false,cancelDefault:true,events:{def:"mouseenter,mouseleave",input:"focus,blur",widget:"focus mouseenter,blur mouseleave",
        tooltip:"mouseenter,mouseleave"},layout:"<div/>",tipClass:"tooltip"},addEffect:function(a, b, c) {
        n[a] = [b,c]
    }};
    var n = {toggle:[function(a) {
        var b = this.getConf(),c = this.getTip();
        b = b.opacity;
        b < 1 && c.css({opacity:b});
        c.show();
        a.call()
    },function(a) {
        this.getTip().hide();
        a.call()
    }],fade:[function(a) {
        var b = this.getConf();
        this.getTip().fadeTo(b.fadeInSpeed, b.opacity, a)
    },function(a) {
        this.getTip().fadeOut(this.getConf().fadeOutSpeed, a)
    }]};
    f.fn.tooltip = function(a) {
        var b = this.data("tooltip");
        if (b)return b;
        a = f.extend(true, {}, f.tools.tooltip.conf, a);
        if (typeof a.position == "string")a.position = a.position.split(/,?\s/);
        this.each(function() {
            b = new t(f(this), a);
            f(this).data("tooltip", b)
        });
        return a.api ? b : this
    }
})(jQuery);
(function(d) {
    var i = d.tools.tooltip;
    d.extend(i.conf, {direction:"up",bounce:false,slideOffset:10,slideInSpeed:200,slideOutSpeed:200,slideFade:!d.browser.msie});
    var e = {up:["-","top"],down:["+","top"],left:["-","left"],right:["+","left"]};
    i.addEffect("slide", function(g) {
        var a = this.getConf(),f = this.getTip(),b = a.slideFade ? {opacity:a.opacity} : {},c = e[a.direction] || e.up;
        b[c[1]] = c[0] + "=" + a.slideOffset;
        a.slideFade && f.css({opacity:0});
        f.show().animate(b, a.slideInSpeed, g)
    }, function(g) {
        var a = this.getConf(),f = a.slideOffset,
                b = a.slideFade ? {opacity:0} : {},c = e[a.direction] || e.up,h = "" + c[0];
        if (a.bounce)h = h == "+" ? "-" : "+";
        b[c[1]] = h + "=" + f;
        this.getTip().animate(b, a.slideOutSpeed, function() {
            d(this).hide();
            g.call()
        })
    })
})(jQuery);
(function(g) {
    function j(a) {
        var c = g(window),d = c.width() + c.scrollLeft(),h = c.height() + c.scrollTop();
        return[a.offset().top <= c.scrollTop(),d <= a.offset().left + a.width(),h <= a.offset().top
                + a.height(),c.scrollLeft() >= a.offset().left]
    }

    function k(a) {
        for (var c = a.length; c--;)if (a[c])return false;
        return true
    }

    var i = g.tools.tooltip;
    i.dynamic = {conf:{classNames:"top right bottom left"}};
    g.fn.dynamic = function(a) {
        if (typeof a == "number")a = {speed:a};
        a = g.extend({}, i.dynamic.conf, a);
        var c = a.classNames.split(/\s/),d;
        this.each(function() {
            var h = g(this).tooltip().onBeforeShow(function(e, f) {
                e = this.getTip();
                var b = this.getConf();
                d || (d = [b.position[0],b.position[1],b.offset[0],b.offset[1],g.extend({}, b)]);
                g.extend(b, d[4]);
                b.position = [d[0],d[1]];
                b.offset = [d[2],d[3]];
                e.css({visibility:"hidden",position:"absolute",top:f.top,left:f.left}).show();
                f = j(e);
                if (!k(f)) {
                    if (f[2]) {
                        g.extend(b, a.top);
                        b.position[0] = "top";
                        e.addClass(c[0])
                    }
                    if (f[3]) {
                        g.extend(b, a.right);
                        b.position[1] = "right";
                        e.addClass(c[1])
                    }
                    if (f[0]) {
                        g.extend(b, a.bottom);
                        b.position[0] = "bottom";
                        e.addClass(c[2])
                    }
                    if (f[1]) {
                        g.extend(b, a.left);
                        b.position[1] = "left";
                        e.addClass(c[3])
                    }
                    if (f[0] || f[2])b.offset[0] *= -1;
                    if (f[1] || f[3])b.offset[1] *= -1
                }
                e.css({visibility:"visible"}).hide()
            });
            h.onBeforeShow(function() {
                var e = this.getConf();
                this.getTip();
                setTimeout(function() {
                    e.position = [d[0],d[1]];
                    e.offset = [d[2],d[3]]
                }, 0)
            });
            h.onHide(function() {
                var e = this.getTip();
                e.removeClass(a.classNames)
            });
            ret = h
        });
        return a.api ? ret : this
    }
})(jQuery);
(function(e) {
    function n(f, c) {
        var a = e(c);
        return a.length < 2 ? a : f.parent().find(c)
    }

    function t(f, c) {
        var a = this,l = f.add(a),g = f.children(),k = 0,m = c.vertical;
        j || (j = a);
        if (g.length > 1)g = e(c.items, f);
        e.extend(a, {getConf:function() {
            return c
        },getIndex:function() {
            return k
        },getSize:function() {
            return a.getItems().size()
        },getNaviButtons:function() {
            return o.add(p)
        },getRoot:function() {
            return f
        },getItemWrap:function() {
            return g
        },getItems:function() {
            return g.children(c.item).not("." + c.clonedClass)
        },move:function(b, d) {
            return a.seekTo(k + b, d)
        },next:function(b) {
            return a.move(1, b)
        },prev:function(b) {
            return a.move(-1, b)
        },begin:function(b) {
            return a.seekTo(0, b)
        },end:function(b) {
            return a.seekTo(a.getSize() - 1, b)
        },focus:function() {
            return j = a
        },addItem:function(b) {
            b = e(b);
            if (c.circular) {
                e(".cloned:last").before(b);
                e(".cloned:first").replaceWith(b.clone().addClass(c.clonedClass))
            } else g.append(b);
            l.trigger("onAddItem", [b]);
            return a
        },seekTo:function(b, d, h) {
            if (c.circular && b === 0 && k == -1 && d !== 0)return a;
            if (!c.circular && b < 0 || b > a.getSize() || b < -1)return a;
            var i = b;
            if (b.jquery)b = a.getItems().index(b); else i = a.getItems().eq(b);
            var q = e.Event("onBeforeSeek");
            if (!h) {
                l.trigger(q, [b,d]);
                if (q.isDefaultPrevented() || !i.length)return a
            }
            i = m ? {top:-i.position().top} : {left:-i.position().left};
            k = b;
            j = a;
            if (d === undefined)d = c.speed;
            g.animate(i, d, c.easing, h || function() {
                l.trigger("onSeek", [b])
            });
            return a
        }});
        e.each(["onBeforeSeek","onSeek","onAddItem"], function(b, d) {
            e.isFunction(c[d]) && e(a).bind(d, c[d]);
            a[d] = function(h) {
                e(a).bind(d, h);
                return a
            }
        });
        if (c.circular) {
            var r = a.getItems().slice(-1).clone().prependTo(g),
                    s = a.getItems().eq(1).clone().appendTo(g);
            r.add(s).addClass(c.clonedClass);
            a.onBeforeSeek(function(b, d, h) {
                if (!b.isDefaultPrevented())if (d == -1) {
                    a.seekTo(r, h, function() {
                        a.end(0)
                    });
                    return b.preventDefault()
                } else d == a.getSize() && a.seekTo(s, h, function() {
                    a.begin(0)
                })
            });
            a.seekTo(0, 0)
        }
        var o = n(f, c.prev).click(function() {
            a.prev()
        }),p = n(f, c.next).click(function() {
            a.next()
        });
        !c.circular && a.getSize() > 1 && a.onBeforeSeek(function(b, d) {
            o.toggleClass(c.disabledClass, d <= 0);
            p.toggleClass(c.disabledClass, d >= a.getSize() - 1)
        });
        c.mousewheel && e.fn.mousewheel && f.mousewheel(function(b, d) {
            if (c.mousewheel) {
                a.move(d < 0 ? 1 : -1, c.wheelSpeed || 50);
                return false
            }
        });
        c.keyboard && e(document).bind("keydown.scrollable", function(b) {
            if (!(!c.keyboard || b.altKey || b.ctrlKey || e(b.target).is(":input")))if (!(c.keyboard != "static" && j
                    != a)) {
                var d = b.keyCode;
                if (m && (d == 38 || d == 40)) {
                    a.move(d == 38 ? -1 : 1);
                    return b.preventDefault()
                }
                if (!m && (d == 37 || d == 39)) {
                    a.move(d == 37 ? -1 : 1);
                    return b.preventDefault()
                }
            }
        });
        e(a).trigger("onBeforeSeek", [c.initialIndex])
    }

    e.tools = e.tools || {version:"1.2.2"};
    e.tools.scrollable
            = {conf:{activeClass:"active",circular:false,clonedClass:"cloned",disabledClass:"disabled",easing:"swing",initialIndex:0,item:null,items:".items",keyboard:true,mousewheel:false,next:".next",prev:".prev",speed:400,vertical:false,wheelSpeed:0}};
    var j;
    e.fn.scrollable = function(f) {
        var c = this.data("scrollable");
        if (c)return c;
        f = e.extend({}, e.tools.scrollable.conf, f);
        this.each(function() {
            c = new t(e(this), f);
            e(this).data("scrollable", c)
        });
        return f.api ? c : this
    }
})(jQuery);
(function(c) {
    var g = c.tools.scrollable;
    g.autoscroll = {conf:{autoplay:true,interval:3E3,autopause:true}};
    c.fn.autoscroll = function(d) {
        if (typeof d == "number")d = {interval:d};
        var b = c.extend({}, g.autoscroll.conf, d),h;
        this.each(function() {
            var a = c(this).data("scrollable");
            if (a)h = a;
            var e,i,f = true;
            a.play = function() {
                if (!e) {
                    f = false;
                    e = setInterval(function() {
                        a.next()
                    }, b.interval);
                    a.next()
                }
            };
            a.pause = function() {
                e = clearInterval(e)
            };
            a.stop = function() {
                a.pause();
                f = true
            };
            b.autopause && a.getRoot().add(a.getNaviButtons()).hover(function() {
                a.pause();
                clearInterval(i)
            }, function() {
                f || (i = setTimeout(a.play, b.interval))
            });
            b.autoplay && setTimeout(a.play, b.interval)
        });
        return b.api ? h : this
    }
})(jQuery);
(function(d) {
    function p(c, g) {
        var h = d(g);
        return h.length < 2 ? h : c.parent().find(g)
    }

    var m = d.tools.scrollable;
    m.navigator = {conf:{navi:".navi",naviItem:null,activeClass:"active",indexed:false,idPrefix:null,history:false}};
    d.fn.navigator = function(c) {
        if (typeof c == "string")c = {navi:c};
        c = d.extend({}, m.navigator.conf, c);
        var g;
        this.each(function() {
            function h(a, b, i) {
                e.seekTo(b);
                if (j) {
                    if (location.hash)location.hash = a.attr("href").replace("#", "")
                } else return i.preventDefault()
            }

            function f() {
                return k.find(c.naviItem || "> *")
            }

            function n(a) {
                var b = d("<" + (c.naviItem || "a") + "/>").click(function(i) {
                    h(d(this), a, i)
                }).attr("href", "#" + a);
                a === 0 && b.addClass(l);
                c.indexed && b.text(a + 1);
                c.idPrefix && b.attr("id", c.idPrefix + a);
                return b.appendTo(k)
            }

            function o(a, b) {
                a = f().eq(b.replace("#", ""));
                a.length || (a = f().filter("[href=" + b + "]"));
                a.click()
            }

            var e = d(this).data("scrollable"),k = p(e.getRoot(), c.navi),q = e.getNaviButtons(),l = c.activeClass,j = c.history
                    && d.fn.history;
            if (e)g = e;
            e.getNaviButtons = function() {
                return q.add(k)
            };
            f().length ? f().each(function(a) {
                d(this).click(function(b) {
                    h(d(this), a, b)
                })
            }) : d.each(e.getItems(), function(a) {
                n(a)
            });
            e.onBeforeSeek(function(a, b) {
                var i = f().eq(b);
                !a.isDefaultPrevented() && i.length && f().removeClass(l).eq(b).addClass(l)
            });
            e.onAddItem(function(a, b) {
                b = n(e.getItems().index(b));
                j && b.history(o)
            });
            j && f().history(o)
        });
        return c.api ? g : this
    }
})(jQuery);
(function(a) {
    function t(d, b) {
        var c = this,i = d.add(c),o = a(window),k,f,m,g = a.tools.expose && (b.mask
                || b.expose),n = Math.random().toString().slice(10);
        if (g) {
            if (typeof g == "string")g = {color:g};
            g.closeOnClick = g.closeOnEsc = false
        }
        var p = b.target || d.attr("rel");
        f = p ? a(p) : d;
        if (!f.length)throw"Could not find Overlay: " + p;
        d && d.index(f) == -1 && d.click(function(e) {
            c.load(e);
            return e.preventDefault()
        });
        a.extend(c, {load:function(e) {
            if (c.isOpened())return c;
            var h = q[b.effect];
            if (!h)throw'Overlay: cannot find effect : "' + b.effect + '"';
            b.oneInstance && a.each(s, function() {
                this.close(e)
            });
            e = e || a.Event();
            e.type = "onBeforeLoad";
            i.trigger(e);
            if (e.isDefaultPrevented())return c;
            m = true;
            g && a(f).expose(g);
            var j = b.top,r = b.left,u = f.outerWidth({margin:true}),v = f.outerHeight({margin:true});
            if (typeof j == "string")j = j == "center" ? Math.max((o.height() - v) / 2, 0) : parseInt(j, 10) / 100
                    * o.height();
            if (r == "center")r = Math.max((o.width() - u) / 2, 0);
            h[0].call(c, {top:j,left:r}, function() {
                if (m) {
                    e.type = "onLoad";
                    i.trigger(e)
                }
            });
            g && b.closeOnClick && a.mask.getMask().one("click", c.close);
            b.closeOnClick && a(document).bind("click." + n, function(l) {
                a(l.target).parents(f).length || c.close(l)
            });
            b.closeOnEsc && a(document).bind("keydown." + n, function(l) {
                l.keyCode == 27 && c.close(l)
            });
            return c
        },close:function(e) {
            if (!c.isOpened())return c;
            e = e || a.Event();
            e.type = "onBeforeClose";
            i.trigger(e);
            if (!e.isDefaultPrevented()) {
                m = false;
                q[b.effect][1].call(c, function() {
                    e.type = "onClose";
                    i.trigger(e)
                });
                a(document).unbind("click." + n).unbind("keydown." + n);
                g && a.mask.close();
                return c
            }
        },getOverlay:function() {
            return f
        },
            getTrigger:function() {
                return d
            },getClosers:function() {
                return k
            },isOpened:function() {
                return m
            },getConf:function() {
                return b
            }});
        a.each("onBeforeLoad,onStart,onLoad,onBeforeClose,onClose".split(","), function(e, h) {
            a.isFunction(b[h]) && a(c).bind(h, b[h]);
            c[h] = function(j) {
                a(c).bind(h, j);
                return c
            }
        });
        k = f.find(b.close || ".close");
        if (!k.length && !b.close) {
            k = a('<div class="close"></div>');
            f.prepend(k)
        }
        k.click(function(e) {
            c.close(e)
        });
        b.load && c.load()
    }

    a.tools = a.tools || {version:"1.2.2"};
    a.tools.overlay = {addEffect:function(d, b, c) {
        q[d] = [b,c]
    },conf:{close:null,closeOnClick:true,closeOnEsc:true,closeSpeed:"fast",effect:"default",fixed:!a.browser.msie
            || a.browser.version
            > 6,left:"center",load:false,mask:null,oneInstance:true,speed:"normal",target:null,top:"10%"}};
    var s = [],q = {};
    a.tools.overlay.addEffect("default", function(d, b) {
        var c = this.getConf(),i = a(window);
        if (!c.fixed) {
            d.top += i.scrollTop();
            d.left += i.scrollLeft()
        }
        d.position = c.fixed ? "fixed" : "absolute";
        this.getOverlay().css(d).fadeIn(c.speed, b)
    }, function(d) {
        this.getOverlay().fadeOut(this.getConf().closeSpeed, d)
    });
    a.fn.overlay = function(d) {
        var b = this.data("overlay");
        if (b)return b;
        if (a.isFunction(d))d = {onBeforeLoad:d};
        d = a.extend(true, {}, a.tools.overlay.conf, d);
        this.each(function() {
            b = new t(a(this), d);
            s.push(b);
            a(this).data("overlay", b)
        });
        return d.api ? b : this
    }
})(jQuery);
(function(i) {
    function j(b) {
        var d = b.offset();
        return{top:d.top + b.height() / 2,left:d.left + b.width() / 2}
    }

    var k = i.tools.overlay,f = i(window);
    i.extend(k.conf, {start:{top:null,left:null},fadeInSpeed:"fast",zIndex:9999});
    function n(b, d) {
        var a = this.getOverlay(),c = this.getConf(),g = this.getTrigger(),o = this,l = a.outerWidth({margin:true}),h = a.data("img");
        if (!h) {
            var e = a.css("backgroundImage");
            if (!e)throw"background-image CSS property not set for overlay";
            e = e.slice(e.indexOf("(") + 1, e.indexOf(")")).replace(/\"/g, "");
            a.css("backgroundImage", "none");
            h = i('<img src="' + e + '"/>');
            h.css({border:0,display:"none"}).width(l);
            i("body").append(h);
            a.data("img", h)
        }
        e = c.start.top || Math.round(f.height() / 2);
        var m = c.start.left || Math.round(f.width() / 2);
        if (g) {
            g = j(g);
            e = g.top;
            m = g.left
        }
        h.css({position:"absolute",top:e,left:m,width:0,zIndex:c.zIndex}).show();
        b.top += f.scrollTop();
        b.left += f.scrollLeft();
        b.position = "absolute";
        a.css(b);
        h.animate({top:a.css("top"),left:a.css("left"),width:l}, c.speed, function() {
            if (c.fixed) {
                b.top -= f.scrollTop();
                b.left -= f.scrollLeft();
                b.position = "fixed";
                h.add(a).css(b)
            }
            a.css("zIndex", c.zIndex + 1).fadeIn(c.fadeInSpeed, function() {
                o.isOpened() && !i(this).index(a) ? d.call() : a.hide()
            })
        })
    }

    function p(b) {
        var d = this.getOverlay().hide(),a = this.getConf(),c = this.getTrigger();
        d = d.data("img");
        var g = {top:a.start.top,left:a.start.left,width:0};
        c && i.extend(g, j(c));
        a.fixed && d.css({position:"absolute"}).animate({top:"+=" + f.scrollTop(),left:"+=" + f.scrollLeft()}, 0);
        d.animate(g, a.closeSpeed, b)
    }

    k.addEffect("apple", n, p)
})(jQuery);
(function(d) {
    function R(b, c) {
        return 32 - (new Date(b, c, 32)).getDate()
    }

    function S(b, c) {
        b = "" + b;
        for (c = c || 2; b.length < c;)b = "0" + b;
        return b
    }

    function T(b, c, j) {
        var m = b.getDate(),h = b.getDay(),t = b.getMonth();
        b = b.getFullYear();
        var f = {d:m,dd:S(m),ddd:B[j].shortDays[h],dddd:B[j].days[h],m:t + 1,mm:S(t
                + 1),mmm:B[j].shortMonths[t],mmmm:B[j].months[t],yy:String(b).slice(2),yyyy:b};
        c = c.replace(X, function(o) {
            return o in f ? f[o] : o.slice(1, o.length - 1)
        });
        return Y.html(c).html()
    }

    function y(b) {
        return parseInt(b, 10)
    }

    function U(b, c) {
        return b.getYear() === c.getYear() && b.getMonth() == c.getMonth() && b.getDate() == c.getDate()
    }

    function C(b) {
        if (b) {
            if (b.constructor == Date)return b;
            if (typeof b == "string") {
                var c = b.split("-");
                if (c.length == 3)return new Date(y(c[0]), y(c[1]) - 1, y(c[2]));
                if (!/^-?\d+$/.test(b))return;
                b = y(b)
            }
            c = new Date;
            c.setDate(c.getDate() + b);
            return c
        }
    }

    function Z(b, c) {
        function j(a, e, g) {
            l = a;
            D = a.getFullYear();
            E = a.getMonth();
            G = a.getDate();
            g = g || d.Event("api");
            g.type = "change";
            H.trigger(g, [a]);
            if (!g.isDefaultPrevented()) {
                b.val(T(a, e.format, e.lang));
                b.data("date", a);
                h.hide(g)
            }
        }

        function m(a) {
            a.type = "onShow";
            H.trigger(a);
            d(document).bind("keydown.d", function(e) {
                var g = e.keyCode;
                if (g == 8) {
                    b.val("");
                    return h.hide(e)
                }
                if (g == 27)return h.hide(e);
                if (d(V).index(g) >= 0) {
                    if (!u) {
                        h.show(e);
                        return e.preventDefault()
                    }
                    var i = d("#" + f.weeks + " a"),p = d("." + f.focus),q = i.index(p);
                    p.removeClass(f.focus);
                    if (g == 74 || g == 40)q += 7; else if (g == 75 || g == 38)q -= 7; else if (g == 76 || g == 39)q
                            += 1; else if (g == 72 || g == 37)q -= 1;
                    if (q == -1) {
                        h.addMonth(-1);
                        p = d("#" + f.weeks + " a:last")
                    } else if (q == 35) {
                        h.addMonth();
                        p = d("#" + f.weeks + " a:first")
                    } else p = i.eq(q);
                    p.addClass(f.focus);
                    return e.preventDefault()
                }
                if (g == 34)return h.addMonth();
                if (g == 33)return h.addMonth(-1);
                if (g == 36)return h.today();
                if (g == 13)d(e.target).is("select") || d("." + f.focus).click();
                return d([16,17,18,9]).index(g) >= 0
            });
            d(document).bind("click.d", function(e) {
                var g = e.target;
                if (!d(g).parents("#" + f.root).length && g != b[0] && (!K || g != K[0]))h.hide(e)
            })
        }

        var h = this,t = new Date,f = c.css,o = B[c.lang],k = d("#" + f.root),L = k.find("#" + f.title),K,I,J,D,
                E,G,l = b.attr("data-value") || c.value || b.val(),r = b.attr("min") || c.min,s = b.attr("max")
                || c.max,u;
        l = C(l) || t;
        r = C(r || c.yearRange[0] * 365);
        s = C(s || c.yearRange[1] * 365);
        if (!o)throw"Dateinput: invalid language: " + c.lang;
        if (b.attr("type") == "date") {
            var M = d("<input/>");
            d.each("name,readonly,disabled,value,required".split(","), function(a, e) {
                M.attr(e, b.attr(e))
            });
            b.replaceWith(M);
            b = M
        }
        b.addClass(f.input);
        var H = b.add(h);
        if (!k.length) {
            k = d("<div><div><a/><div/><a/></div><div><div/><div/></div></div>").hide().css({position:"absolute"}).attr("id", f.root);
            k.children().eq(0).attr("id", f.head).end().eq(1).attr("id", f.body).children().eq(0).attr("id", f.days).end().eq(1).attr("id", f.weeks).end().end().end().find("a").eq(0).attr("id", f.prev).end().eq(1).attr("id", f.next);
            L = k.find("#" + f.head).find("div").attr("id", f.title);
            if (c.selectors) {
                var z = d("<select/>").attr("id", f.month),A = d("<select/>").attr("id", f.year);
                L.append(z.add(A))
            }
            for (var $ = k.find("#" + f.days),N = 0; N < 7; N++)$.append(d("<span/>").text(o.shortDays[(N + c.firstDay)
                    % 7]));
            b.after(k)
        }
        if (c.trigger)K = d("<a/>").attr("href", "#").addClass(f.trigger).click(function(a) {
            h.show();
            return a.preventDefault()
        }).insertAfter(b);
        var O = k.find("#" + f.weeks);
        A = k.find("#" + f.year);
        z = k.find("#" + f.month);
        d.extend(h, {show:function(a) {
            if (!(b.is("[readonly]") || u)) {
                a = a || d.Event();
                a.type = "onBeforeShow";
                H.trigger(a);
                if (!a.isDefaultPrevented()) {
                    d.each(W, function() {
                        this.hide()
                    });
                    u = true;
                    z.unbind("change").change(function() {
                        h.setValue(A.val(), d(this).val())
                    });
                    A.unbind("change").change(function() {
                        h.setValue(d(this).val(), z.val())
                    });
                    I = k.find("#" + f.prev).unbind("click").click(function() {
                        I.hasClass(f.disabled) || h.addMonth(-1);
                        return false
                    });
                    J = k.find("#" + f.next).unbind("click").click(function() {
                        J.hasClass(f.disabled) || h.addMonth();
                        return false
                    });
                    h.setValue(l);
                    var e = b.position();
                    k.css({top:e.top + b.outerHeight({margins:true}) + c.offset[0],left:e.left + c.offset[1]});
                    if (c.speed)k.show(c.speed, function() {
                        m(a)
                    }); else {
                        k.show();
                        m(a)
                    }
                    return h
                }
            }
        },setValue:function(a, e, g) {
            var i;
            if (parseInt(e, 10) >= -1) {
                a = y(a);
                e = y(e);
                g = y(g);
                i = new Date(a, e, g)
            } else {
                i = a || l;
                a = i.getYear() + 1900;
                e = i.getMonth();
                g = i.getDate()
            }
            if (e == -1) {
                e = 11;
                a--
            } else if (e == 12) {
                e = 0;
                a++
            }
            if (!u) {
                j(i, c);
                return h
            }
            E = e;
            D = a;
            i = new Date(a, e, 1 - c.firstDay);
            g = i.getDay();
            var p = R(a, e),q = R(a, e - 1),P;
            if (c.selectors) {
                z.empty();
                d.each(o.months, function(v, F) {
                    r < new Date(a, v + 1, -1) && s > new Date(a, v, 0)
                    && z.append(d("<option/>").html(F).attr("value", v))
                });
                A.empty();
                for (i = a + c.yearRange[0]; i < a + c.yearRange[1]; i++)r < new Date(i + 1, -1, 0) && s
                        > new Date(i, 0, 0) && A.append(d("<option/>").text(i));
                z.val(e);
                A.val(a)
            } else L.html(o.months[e] + " " + a);
            O.empty();
            I.add(J).removeClass(f.disabled);
            for (var w = 0,n,x; w < 42; w++) {
                n = d("<a/>");
                if (w % 7 === 0) {
                    P = d("<div/>").addClass(f.week);
                    O.append(P)
                }
                if (w < g) {
                    n.addClass(f.off);
                    x = q - g + w + 1;
                    i = new Date(a, e - 1, x)
                } else if (w >= g + p) {
                    n.addClass(f.off);
                    x = w - p - g + 1;
                    i = new Date(a, e + 1, x)
                } else {
                    x = w - g + 1;
                    i = new Date(a, e, x);
                    if (U(l, i))n.attr("id", f.current).addClass(f.focus); else U(t, i) && n.attr("id", f.today)
                }
                r && i < r && n.add(I).addClass(f.disabled);
                s && i > s && n.add(J).addClass(f.disabled);
                n.attr("href", "#" + x).text(x).data("date", i);
                P.append(n);
                n.click(function(v) {
                    var F = d(this);
                    if (!F.hasClass(f.disabled)) {
                        d("#" + f.current).removeAttr("id");
                        F.attr("id", f.current);
                        j(F.data("date"), c, v)
                    }
                    return false
                })
            }
            f.sunday && O.find(f.week).each(function() {
                var v = c.firstDay ? 7 - c.firstDay : 0;
                d(this).children().slice(v, v + 1).addClass(f.sunday)
            });
            return h
        },setMin:function(a, e) {
            r = C(a);
            e && l < r && h.setValue(r);
            return h
        },setMax:function(a, e) {
            s = C(a);
            e && l > s && h.setValue(s);
            return h
        },today:function() {
            return h.setValue(t)
        },addDay:function(a) {
            return this.setValue(D, E, G + (a || 1))
        },addMonth:function(a) {
            return this.setValue(D, E + (a || 1), G)
        },addYear:function(a) {
            return this.setValue(D + (a || 1), E, G)
        },hide:function(a) {
            if (u) {
                a = a || d.Event();
                a.type = "onHide";
                H.trigger(a);
                d(document).unbind("click.d").unbind("keydown.d");
                if (a.isDefaultPrevented())return;
                k.hide();
                u = false
            }
            return h
        },getConf:function() {
            return c
        },getInput:function() {
            return b
        },getCalendar:function() {
            return k
        },getValue:function(a) {
            return a ? T(l, a, c.lang) : l
        },isOpen:function() {
            return u
        }});
        d.each(["onBeforeShow","onShow","change",
            "onHide"], function(a, e) {
            d.isFunction(c[e]) && d(h).bind(e, c[e]);
            h[e] = function(g) {
                d(h).bind(e, g);
                return h
            }
        });
        b.bind("focus click", h.show).keydown(function(a) {
            var e = a.keyCode;
            if (!u && d(V).index(e) >= 0) {
                h.show(a);
                return a.preventDefault()
            }
            return a.shiftKey || a.ctrlKey || a.altKey || e == 9 ? true : a.preventDefault()
        });
        C(b.val()) && j(l, c)
    }

    d.tools = d.tools || {version:"1.2.2"};
    var W = [],Q,V = [75,76,38,39,74,72,40,37],B = {};
    Q = d.tools.dateinput = {conf:{format:"mm/dd/yy",selectors:false,yearRange:[-5,5],lang:"en",offset:[0,0],
        speed:0,firstDay:0,min:0,max:0,trigger:false,css:{prefix:"cal",input:"date",root:0,head:0,title:0,prev:0,next:0,month:0,year:0,days:0,body:0,weeks:0,today:0,current:0,week:0,off:0,sunday:0,focus:0,disabled:0,trigger:0}},localize:function(
            b, c) {
        d.each(c, function(j, m) {
            c[j] = m.split(",")
        });
        B[b] = c
    }};
    Q.localize("en", {months:"January,February,March,April,May,June,July,August,September,October,November,December",shortMonths:"Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec",days:"Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday",
        shortDays:"Sun,Mon,Tue,Wed,Thu,Fri,Sat"});
    var X = /d{1,4}|m{1,4}|yy(?:yy)?|"[^"]*"|'[^']*'/g,Y = d("<a/>");
    d.expr[":"].date = function(b) {
        var c = b.getAttribute("type");
        return c && c == "date" || !!d(b).data("dateinput")
    };
    d.fn.dateinput = function(b) {
        if (this.data("dateinput"))return this;
        b = d.extend({}, Q.conf, b);
        d.each(b.css, function(j, m) {
            if (!m && j != "prefix")b.css[j] = (b.css.prefix || "") + (m || j)
        });
        var c;
        this.each(function() {
            var j = new Z(d(this), b);
            W.push(j);
            j = j.getInput().data("dateinput", j);
            c = c ? c.add(j) : j
        });
        return c ? c : this
    }
})(jQuery);
(function(e) {
    function F(d, a) {
        a = Math.pow(10, a);
        return Math.round(d * a) / a
    }

    function p(d, a) {
        if (a = parseInt(d.css(a), 10))return a;
        return(d = d[0].currentStyle) && d.width && parseInt(d.width, 10)
    }

    function C(d) {
        return(d = d.data("events")) && d.onSlide
    }

    function G(d, a) {
        function h(c, b, f, j) {
            if (f === undefined)f = b / k * z; else if (j)f -= a.min;
            if (r)f = Math.round(f / r) * r;
            if (b === undefined || r)b = f * k / z;
            if (isNaN(f))return g;
            b = Math.max(0, Math.min(b, k));
            f = b / k * z;
            if (j || !n)f += a.min;
            if (n)if (j)b = k - b; else f = a.max - f;
            f = F(f, t);
            var q = c.type == "click";
            if (D && l !== undefined && !q) {
                c.type = "onSlide";
                A.trigger(c, [f,b]);
                if (c.isDefaultPrevented())return g
            }
            j = q ? a.speed : 0;
            q = q ? function() {
                c.type = "change";
                A.trigger(c, [f])
            } : null;
            if (n) {
                m.animate({top:b}, j, q);
                a.progress && B.animate({height:k - b + m.width() / 2}, j)
            } else {
                m.animate({left:b}, j, q);
                a.progress && B.animate({width:b + m.width() / 2}, j)
            }
            l = f;
            H = b;
            d.val(f);
            return g
        }

        function s() {
            if (n = a.vertical || p(i, "height") > p(i, "width")) {
                k = p(i, "height") - p(m, "height");
                u = i.offset().top + k
            } else {
                k = p(i, "width") - p(m, "width");
                u = i.offset().left
            }
        }

        function v() {
            s();
            g.setValue(a.value || a.min)
        }

        var g = this,o = a.css,i = e("<div><div/><a href='#'/></div>").data("rangeinput", g),n,l,u,k,H;
        d.before(i);
        var m = i.addClass(o.slider).find("a").addClass(o.handle),B = i.find("div").addClass(o.progress);
        e.each("min,max,step,value".split(","), function(c, b) {
            c = d.attr(b);
            if (parseFloat(c))a[b] = parseFloat(c, 10)
        });
        var z = a.max - a.min,r = a.step == "any" ? 0 : a.step,t = a.precision;
        if (t === undefined)try {
            t = r.toString().split(".")[1].length
        } catch(I) {
            t = 0
        }
        if (d.attr("type") == "range") {
            var w = e("<input/>");
            e.each("name,readonly,disabled,required".split(","), function(c, b) {
                w.attr(b, d.attr(b))
            });
            w.val(a.value);
            d.replaceWith(w);
            d = w
        }
        d.addClass(o.input);
        var A = e(g).add(d),D = true;
        e.extend(g, {getValue:function() {
            return l
        },setValue:function(c, b) {
            return h(b || e.Event("api"), undefined, c, true)
        },getConf:function() {
            return a
        },getProgress:function() {
            return B
        },getHandle:function() {
            return m
        },getInput:function() {
            return d
        },step:function(c, b) {
            b = b || e.Event();
            var f = a.step == "any" ? 1 : a.step;
            g.setValue(l + f * (c || 1), b)
        },stepUp:function(c) {
            return g.step(c || 1)
        },stepDown:function(c) {
            return g.step(-c || -1)
        }});
        e.each("onSlide,change".split(","), function(c, b) {
            e.isFunction(a[b]) && e(g).bind(b, a[b]);
            g[b] = function(f) {
                e(g).bind(b, f);
                return g
            }
        });
        m.drag({drag:false}).bind("dragStart", function() {
            D = C(e(g)) || C(d)
        }).bind("drag", function(c, b, f) {
            if (d.is(":disabled"))return false;
            h(c, n ? b : f)
        }).bind("dragEnd", function(c) {
            if (!c.isDefaultPrevented()) {
                c.type = "change";
                A.trigger(c, [l])
            }
        }).click(function(c) {
            return c.preventDefault()
        });
        i.click(function(c) {
            if (d.is(":disabled") || c.target == m[0])return c.preventDefault();
            s();
            var b = m.width() / 2;
            h(c, n ? k - u - b + c.pageY : c.pageX - u - b)
        });
        a.keyboard && d.keydown(function(c) {
            if (!d.attr("readonly")) {
                var b = c.keyCode,f = e([75,76,38,33,39]).index(b) != -1,j = e([74,72,40,34,37]).index(b) != -1;
                if ((f || j) && !(c.shiftKey || c.altKey || c.ctrlKey)) {
                    if (f)g.step(b == 33 ? 10 : 1, c); else if (j)g.step(b == 34 ? -10 : -1, c);
                    return c.preventDefault()
                }
            }
        });
        d.blur(function(c) {
            var b = e(this).val();
            b !== l && g.setValue(b, c)
        });
        e.extend(d[0], {stepUp:g.stepUp,stepDown:g.stepDown});
        v();
        k || e(window).load(v)
    }

    e.tools = e.tools || {version:"1.2.2"};
    var E;
    E = e.tools.rangeinput
            = {conf:{min:0,max:100,step:"any",steps:0,value:0,precision:undefined,vertical:0,keyboard:true,progress:false,speed:100,css:{input:"range",slider:"slider",progress:"progress",handle:"handle"}}};
    var x,y;
    e.fn.drag = function(d) {
        document.ondragstart = function() {
            return false
        };
        d = e.extend({x:true,y:true,drag:true}, d);
        x = x || e(document).bind("mousedown mouseup", function(a) {
            var h = e(a.target);
            if (a.type == "mousedown" && h.data("drag")) {
                var s = h.position(),v = a.pageX - s.left,g = a.pageY - s.top,o = true;
                x.bind("mousemove.drag", function(i) {
                    var n = i.pageX - v;
                    i = i.pageY - g;
                    var l = {};
                    if (d.x)l.left = n;
                    if (d.y)l.top = i;
                    if (o) {
                        h.trigger("dragStart");
                        o = false
                    }
                    d.drag && h.css(l);
                    h.trigger("drag", [i,n]);
                    y = h
                });
                a.preventDefault()
            } else try {
                y && y.trigger("dragEnd")
            } finally {
                x.unbind("mousemove.drag");
                y = null
            }
        });
        return this.data("drag", true)
    };
    e.expr[":"].range = function(d) {
        var a = d.getAttribute("type");
        return a && a == "range" || !!e(d).filter("input").data("rangeinput")
    };
    e.fn.rangeinput = function(d) {
        if (this.data("rangeinput"))return this;
        d = e.extend(true, {}, E.conf, d);
        var a;
        this.each(function() {
            var h = new G(e(this), e.extend(true, {}, d));
            h = h.getInput().data("rangeinput", h);
            a = a ? a.add(h) : h
        });
        return a ? a : this
    }
})(jQuery);
(function(e) {
    function v(a, b, c) {
        var j = a.offset().top,g = a.offset().left,l = c.position.split(/,?\s+/),f = l[0];
        l = l[1];
        j -= b.outerHeight() - c.offset[0];
        g += a.outerWidth() + c.offset[1];
        c = b.outerHeight() + a.outerHeight();
        if (f == "center")j += c / 2;
        if (f == "bottom")j += c;
        a = a.outerWidth();
        if (l == "center")g -= (a + b.outerWidth()) / 2;
        if (l == "left")g -= a;
        return{top:j,left:g}
    }

    function w(a) {
        function b() {
            return this.getAttribute("type") == a
        }

        b.key = "[type=" + a + "]";
        return b
    }

    function s(a, b, c) {
        function j(f, d, k) {
            if (!(!c.grouped && f.length)) {
                var h;
                if (k === false || e.isArray(k)) {
                    h = i.messages[d.key || d] || i.messages["*"];
                    h = h[c.lang] || i.messages["*"].en;
                    (d = h.match(/\$\d/g)) && e.isArray(k) && e.each(d, function(n) {
                        h = h.replace(this, k[n])
                    })
                } else h = k[c.lang] || k;
                f.push(h)
            }
        }

        var g = this,l = b.add(g);
        a = a.not(":button, :image, :reset, :submit");
        e.extend(g, {getConf:function() {
            return c
        },getForm:function() {
            return b
        },getInputs:function() {
            return a
        },invalidate:function(f, d) {
            if (!d) {
                var k = [];
                e.each(f, function(h, n) {
                    h = a.filter("[name=" + h + "]");
                    if (h.length) {
                        h.trigger("OI", [n]);
                        k.push({input:h,messages:[n]})
                    }
                });
                f = k;
                d = e.Event()
            }
            d.type = "onFail";
            l.trigger(d, [f]);
            d.isDefaultPrevented() || q[c.effect][0].call(g, f, d);
            return g
        },reset:function(f) {
            f = f || a;
            f.removeClass(c.errorClass).each(function() {
                var d = e(this).data("msg.el");
                if (d) {
                    d.remove();
                    e(this).data("msg.el", null)
                }
            })
        },checkValidity:function(f, d) {
            f = f || a;
            f = f.not(":disabled");
            if (!f.length)return true;
            d = d || e.Event();
            d.type = "onBeforeValidate";
            l.trigger(d, [f]);
            if (d.isDefaultPrevented())return d.result;
            var k = [],h = c.errorInputEvent + ".v";
            f.each(function() {
                var p = [],m = e(this).unbind(h).data("messages", p);
                e.each(t, function() {
                    var o = this,r = o[0];
                    if (m.filter(r).length) {
                        o = o[1].call(g, m, m.val());
                        if (o !== true) {
                            d.type = "onBeforeFail";
                            l.trigger(d, [m,r]);
                            if (d.isDefaultPrevented())return false;
                            var u = m.attr(c.messageAttr);
                            if (u) {
                                p = [u];
                                return false
                            } else j(p, r, o)
                        }
                    }
                });
                if (p.length) {
                    k.push({input:m,messages:p});
                    m.trigger("OI", [p]);
                    c.errorInputEvent && m.bind(h, function(o) {
                        g.checkValidity(m, o)
                    })
                }
                if (c.singleError && k.length)return false
            });
            var n = q[c.effect];
            if (!n)throw'Validator: cannot find effect "' + c.effect + '"';
            if (k.length) {
                g.invalidate(k, d);
                return false
            } else {
                n[1].call(g, f, d);
                d.type = "onSuccess";
                l.trigger(d, [f]);
                f.unbind(h)
            }
            return true
        }});
        e.each("onBeforeValidate,onBeforeFail,onFail,onSuccess".split(","), function(f, d) {
            e.isFunction(c[d]) && e(g).bind(d, c[d]);
            g[d] = function(k) {
                e(g).bind(d, k);
                return g
            }
        });
        c.formEvent && b.bind(c.formEvent, function(f) {
            if (!g.checkValidity(null, f))return f.preventDefault()
        });
        b.bind("reset", function() {
            g.reset()
        });
        a[0] && a[0].validity && a.each(function() {
            this.oninvalid = function() {
                return false
            }
        });
        if (b[0])b[0].checkValidity = g.checkValidity;
        c.inputEvent && a.bind(c.inputEvent, function(f) {
            g.checkValidity(e(this), f)
        });
        a.filter(":checkbox, select").filter("[required]").change(function(f) {
            var d = e(this);
            if (this.checked || d.is("select") && e(this).val())q[c.effect][1].call(g, d, f)
        })
    }

    e.tools = e.tools || {version:"1.2.2"};
    var x = /\[type=([a-z]+)\]/,y = /^-?[0-9]*(\.[0-9]+)?$/,z = /^([a-z0-9_\.\-\+]+)@([\da-z\.\-]+)\.([a-z\.]{2,6})$/i,A = /^(https?:\/\/)?([\da-z\.\-]+)\.([a-z\.]{2,6})([\/\w \.\-]*)*\/?$/i,
            i;
    i = e.tools.validator
            = {conf:{grouped:false,effect:"default",errorClass:"invalid",inputEvent:null,errorInputEvent:"keyup",formEvent:"submit",lang:"en",message:"<div/>",messageAttr:"data-message",messageClass:"error",offset:[0,0],position:"center right",singleError:false,speed:"normal"},messages:{"*":{en:"Please correct this value"}},localize:function(
            a, b) {
        e.each(b, function(c, j) {
            i.messages[c] = i.messages[c] || {};
            i.messages[c][a] = j
        })
    },localizeFn:function(a, b) {
        i.messages[a] = i.messages[a] || {};
        e.extend(i.messages[a], b)
    },fn:function(a, b, c) {
        if (e.isFunction(b))c = b; else {
            if (typeof b == "string")b = {en:b};
            this.messages[a.key || a] = b
        }
        if (b = x.exec(a))a = w(b[1]);
        t.push([a,c])
    },addEffect:function(a, b, c) {
        q[a] = [b,c]
    }};
    var t = [],q = {"default":[function(a) {
        var b = this.getConf();
        e.each(a, function(c, j) {
            c = j.input;
            c.addClass(b.errorClass);
            var g = c.data("msg.el");
            if (!g) {
                g = e(b.message).addClass(b.messageClass).appendTo(document.body);
                c.data("msg.el", g)
            }
            g.css({visibility:"hidden"}).find("span").remove();
            e.each(j.messages, function(l, f) {
                e("<span/>").html(f).appendTo(g)
            });
            g.outerWidth() == g.parent().width() && g.add(g.find("p")).css({display:"inline"});
            j = v(c, g, b);
            g.css({visibility:"visible",position:"absolute",top:j.top,left:j.left}).fadeIn(b.speed)
        })
    },function(a) {
        var b = this.getConf();
        a.removeClass(b.errorClass).each(function() {
            var c = e(this).data("msg.el");
            c && c.css({visibility:"hidden"})
        })
    }]};
    e.each("email,url,number".split(","), function(a, b) {
        e.expr[":"][b] = function(c) {
            return c.getAttribute("type") === b
        }
    });
    e.fn.oninvalid = function(a) {
        return this[a ? "bind" : "trigger"]("OI", a)
    };
    i.fn(":email", "Please enter a valid email address", function(a, b) {
        return!b || z.test(b)
    });
    i.fn(":url", "Please enter a valid URL", function(a, b) {
        return!b || A.test(b)
    });
    i.fn(":number", "Please enter a numeric value.", function(a, b) {
        return y.test(b)
    });
    i.fn("[max]", "Please enter a value smaller than $1", function(a, b) {
        a = a.attr("max");
        return parseFloat(b) <= parseFloat(a) ? true : [a]
    });
    i.fn("[min]", "Please enter a value larger than $1", function(a, b) {
        a = a.attr("min");
        return parseFloat(b) >= parseFloat(a) ? true : [a]
    });
    i.fn("[required]", "Please complete this mandatory field.", function(a, b) {
        if (a.is(":checkbox"))return a.is(":checked");
        return!!b
    });
    i.fn("[pattern]", function(a) {
        var b = new RegExp("^" + a.attr("pattern") + "$");
        return b.test(a.val())
    });
    e.fn.validator = function(a) {
        if (this.data("validator"))return this;
        a = e.extend(true, {}, i.conf, a);
        if (this.is("form"))return this.each(function() {
            var c = e(this),j = new s(c.find(":input"), c, a);
            c.data("validator", j)
        }); else {
            var b = new s(this, this.eq(0).closest("form"), a);
            return this.data("validator", b)
        }
    }
})(jQuery);
(function() {
    function f(a, b) {
        if (b)for (key in b)if (b.hasOwnProperty(key))a[key] = b[key];
        return a
    }

    function l(a, b) {
        var c = [];
        for (var d in a)if (a.hasOwnProperty(d))c[d] = b(a[d]);
        return c
    }

    function m(a, b, c) {
        if (e.isSupported(b.version))a.innerHTML = e.getHTML(b, c); else if (b.expressInstall
                && e.isSupported([6,65]))a.innerHTML
                = e.getHTML(f(b, {src:b.expressInstall}), {MMredirectURL:location.href,MMplayerType:"PlugIn",MMdoctitle:document.title}); else {
            if (!a.innerHTML.replace(/\s/g, "")) {
                a.innerHTML = "<h2>Flash version " + b.version + " or greater is required</h2><h3>" + (g[0] > 0
                        ? "Your version is " + g : "You have no flash plugin installed") + "</h3>" + (a.tagName == "A"
                        ? "<p>Click here to download latest version</p>" : "<p>Download latest version from <a href='"
                        + k + "'>here</a></p>");
                if (a.tagName == "A")a.onclick = function() {
                    location.href = k
                }
            }
            if (b.onFail) {
                var d = b.onFail.call(this);
                if (typeof d == "string")a.innerHTML = d
            }
        }
        if (h)window[b.id] = document.getElementById(b.id);
        f(this, {getRoot:function() {
            return a
        },getOptions:function() {
            return b
        },getConf:function() {
            return c
        },
            getApi:function() {
                return a.firstChild
            }})
    }

    var h = document.all,k = "http://www.adobe.com/go/getflashplayer",n = typeof jQuery
            == "function",o = /(\d+)[^\d]+(\d+)[^\d]*(\d*)/,i = {width:"100%",height:"100%",id:"_" + (""
            + Math.random()).slice(9),allowfullscreen:true,allowscriptaccess:"always",quality:"high",version:[3,0],onFail:null,expressInstall:null,w3c:false,cachebusting:false};
    window.attachEvent && window.attachEvent("onbeforeunload", function() {
        __flash_unloadHandler = function() {
        };
        __flash_savedUnloadHandler = function() {
        }
    });
    window.flashembed = function(a, b, c) {
        if (typeof a == "string")a = document.getElementById(a.replace("#", ""));
        if (a) {
            if (typeof b == "string")b = {src:b};
            return new m(a, f(f({}, i), b), c)
        }
    };
    var e = f(window.flashembed, {conf:i,getVersion:function() {
        var a;
        try {
            a = navigator.plugins["Shockwave Flash"].description.slice(16)
        } catch(b) {
            try {
                var c = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
                a = c && c.GetVariable("$version")
            } catch(d) {
            }
        }
        return(a = o.exec(a)) ? [a[1],a[3]] : [0,0]
    },asString:function(a) {
        if (a === null || a === undefined)return null;
        var b = typeof a;
        if (b == "object" && a.push)b = "array";
        switch (b) {case "string":a = a.replace(new RegExp('(["\\\\])', "g"), "\\$1");a
                = a.replace(/^\s?(\d+\.?\d+)%/, "$1pct");return'"' + a + '"';case "array":return"[" + l(a, function(d) {
            return e.asString(d)
        }).join(",") + "]";case "function":return'"function()"';case "object":b = [];for (var c in a)a.hasOwnProperty(c)
        && b.push('"' + c + '":' + e.asString(a[c]));return"{" + b.join(",") + "}"
        }
        return String(a).replace(/\s/g, " ").replace(/\'/g, '"')
    },getHTML:function(a, b) {
        a = f({}, a);
        var c = '<object width="' + a.width + '" height="' + a.height + '" id="' + a.id + '" name="' + a.id + '"';
        if (a.cachebusting)a.src += (a.src.indexOf("?") != -1 ? "&" : "?") + Math.random();
        c += a.w3c || !h ? ' data="' + a.src + '" type="application/x-shockwave-flash"'
                : ' classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"';
        c += ">";
        if (a.w3c || h)c += '<param name="movie" value="' + a.src + '" />';
        a.width = a.height = a.id = a.w3c = a.src = null;
        a.onFail = a.version = a.expressInstall = null;
        for (var d in a)if (a[d])c += '<param name="' + d + '" value="' + a[d] + '" />';
        a = "";
        if (b) {
            for (var j in b)if (b[j]) {
                d = b[j];
                a += j + "=" + (/function|object/.test(typeof d) ? e.asString(d) : d) + "&"
            }
            a = a.slice(0, -1);
            c += '<param name="flashvars" value=\'' + a + "' />"
        }
        c += "</object>";
        return c
    },isSupported:function(a) {
        return g[0] > a[0] || g[0] == a[0] && g[1] >= a[1]
    }}),g = e.getVersion();
    if (n) {
        jQuery.tools = jQuery.tools || {version:"1.2.2"};
        jQuery.tools.flashembed = {conf:i};
        jQuery.fn.flashembed = function(a, b) {
            return this.each(function() {
                $(this).data("flashembed", flashembed(this, a, b))
            })
        }
    }
})();
(function(b) {
    function h(c) {
        if (c) {
            var a = d.contentWindow.document;
            a.open().close();
            a.location.hash = c
        }
    }

    var g,d,f,i;
    b.tools = b.tools || {version:"1.2.2"};
    b.tools.history = {init:function(c) {
        if (!i) {
            if (b.browser.msie && b.browser.version < "8") {
                if (!d) {
                    d = b("<iframe/>").attr("src", "javascript:false;").hide().get(0);
                    b("body").append(d);
                    setInterval(function() {
                        var a = d.contentWindow.document;
                        a = a.location.hash;
                        g !== a && b.event.trigger("hash", a)
                    }, 100);
                    h(location.hash || "#")
                }
            } else setInterval(function() {
                var a = location.hash;
                a !== g && b.event.trigger("hash", a)
            }, 100);
            f = !f ? c : f.add(c);
            c.click(function(a) {
                var e = b(this).attr("href");
                d && h(e);
                if (e.slice(0, 1) != "#") {
                    location.href = "#" + e;
                    return a.preventDefault()
                }
            });
            i = true
        }
    }};
    b(window).bind("hash", function(c, a) {
        a ? f.filter(function() {
            var e = b(this).attr("href");
            return e == a || e == a.replace("#", "")
        }).trigger("history", [a]) : f.eq(0).trigger("history", [a]);
        g = a;
        window.location.hash = g
    });
    b.fn.history = function(c) {
        b.tools.history.init(this);
        return this.bind("history", c)
    }
})(jQuery);
(function(b) {
    function k() {
        if (b.browser.msie) {
            var a = b(document).height(),d = b(window).height();
            return[window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth,a - d < 20 ? d
                    : a]
        }
        return[b(window).width(),b(document).height()]
    }

    function h(a) {
        if (a)return a.call(b.mask)
    }

    b.tools = b.tools || {version:"1.2.2"};
    var l;
    l = b.tools.expose
            = {conf:{maskId:"exposeMask",loadSpeed:"slow",closeSpeed:"fast",closeOnClick:true,closeOnEsc:true,zIndex:9998,opacity:0.8,startOpacity:0,color:"#fff",onLoad:null,
        onClose:null}};
    var c,i,f,g,j;
    b.mask = {load:function(a, d) {
        if (f)return this;
        if (typeof a == "string")a = {color:a};
        a = a || g;
        g = a = b.extend(b.extend({}, l.conf), a);
        c = b("#" + a.maskId);
        if (!c.length) {
            c = b("<div/>").attr("id", a.maskId);
            b("body").append(c)
        }
        var m = k();
        c.css({position:"absolute",top:0,left:0,width:m[0],height:m[1],display:"none",opacity:a.startOpacity,zIndex:a.zIndex});
        a.color && c.css("backgroundColor", a.color);
        if (h(a.onBeforeLoad) === false)return this;
        a.closeOnEsc && b(document).bind("keydown.mask", function(e) {
            e.keyCode == 27 && b.mask.close(e)
        });
        a.closeOnClick && c.bind("click.mask", function(e) {
            b.mask.close(e)
        });
        b(window).bind("resize.mask", function() {
            b.mask.fit()
        });
        if (d && d.length) {
            j = d.eq(0).css("zIndex");
            b.each(d, function() {
                var e = b(this);
                /relative|absolute|fixed/i.test(e.css("position")) || e.css("position", "relative")
            });
            i = d.css({zIndex:Math.max(a.zIndex + 1, j == "auto" ? 0 : j)})
        }
        c.css({display:"block"}).fadeTo(a.loadSpeed, a.opacity, function() {
            b.mask.fit();
            h(a.onLoad)
        });
        f = true;
        return this
    },close:function() {
        if (f) {
            if (h(g.onBeforeClose) === false)return this;
            c.fadeOut(g.closeSpeed, function() {
                h(g.onClose);
                i && i.css({zIndex:j})
            });
            b(document).unbind("keydown.mask");
            c.unbind("click.mask");
            b(window).unbind("resize.mask");
            f = false
        }
        return this
    },fit:function() {
        if (f) {
            var a = k();
            c.css({width:a[0],height:a[1]})
        }
    },getMask:function() {
        return c
    },isLoaded:function() {
        return f
    },getConf:function() {
        return g
    },getExposed:function() {
        return i
    }};
    b.fn.mask = function(a) {
        b.mask.load(a);
        return this
    };
    b.fn.expose = function(a) {
        b.mask.load(a, this);
        return this
    }
})(jQuery);
(function(b) {
    function c(a) {
        switch (a.type) {case "mousemove":return b.extend(a.data, {clientX:a.clientX,clientY:a.clientY,pageX:a.pageX,pageY:a.pageY});case "DOMMouseScroll":b.extend(a, a.data);a.delta
                = -a.detail / 3;break;case "mousewheel":a.delta = a.wheelDelta / 120;break
        }
        a.type = "wheel";
        return b.event.handle.call(this, a, a.delta)
    }

    b.fn.mousewheel = function(a) {
        return this[a ? "bind" : "trigger"]("wheel", a)
    };
    b.event.special.wheel = {setup:function() {
        b.event.add(this, d, c, {})
    },teardown:function() {
        b.event.remove(this, d, c)
    }};
    var d = !b.browser.mozilla ? "mousewheel" : "DOMMouseScroll" + (b.browser.version < "1.9" ? " mousemove" : "")
})(jQuery);