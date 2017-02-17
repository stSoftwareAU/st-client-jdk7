<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:media="http://video.search.yahoo.com/mrss"
                xmlns:content="http://purl.org/rss/1.0/modules/content/"
                version="2.0"
                exclude-result-prefixes="dc media content"  
                >
  
  <xsl:output method="html"/>
  <xsl:template match="rss/channel">
    <article class="rssItem">
      <xsl:choose>
        <xsl:when test="item/media:thumbnail/@url != ''">
          <div class="rssHead">
            <div class="rssTitle">
              <h1>
                <xsl:value-of select="item/title"/>
              </h1>
            </div>   
            
            <div class="rssSubTitle">
              <h2>
                <xsl:value-of select="item/media:subTitle"/>
              </h2>
            </div>
            <div class="rssThumbnail">
              <xsl:variable name="rawURL" select="item/media:thumbnail/@url"/>
              <xsl:variable
                            xmlns:encoder="xalan://com.aspc.cms.xlst.Tools"
                            name="imagelink"
                            select="encoder:encodeAnchor( $rawURL)"
                            />
              <xsl:text disable-output-escaping="yes">&lt;img src="</xsl:text>
              <xsl:value-of select="$imagelink"/>
              <xsl:text disable-output-escaping="yes">"/&gt;</xsl:text>
            </div>  
          </div>
        </xsl:when>
        <xsl:otherwise>
          <div class="rssHead" >
            <div class="rssTitle">
              <h1>
                <xsl:value-of select="item/title"/>
              </h1>
            </div>   
            
            <div class="rssSubTitle">
              <h2>
                <xsl:value-of select="item/media:subTitle"/>
              </h2>
            </div>
          </div>
        </xsl:otherwise>
      </xsl:choose>
      <div class="author" style="display: none;">
        <xsl:choose>
          <xsl:when test="item/dc:creator/@avatarurl != ''">
            <div class="authorAvatar">
              <xsl:variable name="avaUrl" select="item/dc:creator/@avatarurl"/>
              <xsl:variable
                            xmlns:encoder="xalan://com.aspc.cms.xlst.Tools"
                            name="avaLink"
                            select="encoder:encodeAnchor( $avaUrl)"
                            />
              <xsl:text disable-output-escaping="yes">&lt;img height="50" src="</xsl:text>
              <xsl:value-of select="$avaLink"/>
              <xsl:text disable-output-escaping="yes">"/&gt;</xsl:text>
            </div>
          </xsl:when>
        </xsl:choose>
        <div class="nameAndTime">
          <span>
            <xsl:text disable-output-escaping="yes">By 
              &lt;a href="/site/ST/blog/?PAGE_NO=1&amp;PAGE_SIZE=20&amp;WHERE_FIELD=authorName&amp;WHERE_VALUE=by%3a</xsl:text>
            <xsl:value-of select="item/dc:creator"/><xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
            <xsl:value-of select="item/dc:creator"/>
            <xsl:text disable-output-escaping="yes">
              &lt;/a&gt;
            </xsl:text>
          </span>
          <span>on
            <xsl:variable name="pd" select="item/pubDate"/>
            <xsl:variable
                          xmlns:encoder="xalan://com.aspc.cms.xlst.Tools"
                          name="formatedPD"
                          select="encoder:formatTime( $pd, 'MMMM dd, yyyy')"
                          />
            <xsl:value-of select="$formatedPD"/>
          </span>
        </div>
        <div class="social" style="display: none;">
          
          <div class="st-twitter">
            <a href="https://twitter.com/share" class="twitter-share-button" data-lang="en"
               data-count="horizontal"
               >Tweet</a>
            <script>!function(d,s,id)
              {var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){
              js=d.createElement(s);js.id=id;js.src="https://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);
              }}
              (document,"script","twitter-wjs");</script>
          </div>
          
          <div class="st-facebook">
            <div id="fb-root"></div>
            <script>(function(d, s, id) {
              var js, fjs = d.getElementsByTagName(s)[0];
              if (d.getElementById(id)) return;
              js = d.createElement(s); js.id = id;
              js.src = "//connect.facebook.net/en_US/all.js#xfbml=1";
              fjs.parentNode.insertBefore(js, fjs);
              }(document, 'script', 'facebook-jssdk'));</script>    
            <div class="fb-like" data-layout="button_count" data-action="recommend" data-show-faces="true" data-share="false"></div>
          </div>
          
          <div class="st-googleplus">
            <div class="g-plus" data-action="share" data-annotation="bubble"></div>
            
            <script type="text/javascript">
              (function() {
              var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
              po.src = 'https://apis.google.com/js/platform.js';
              var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
              })();
            </script>
          </div>
          
          <div class="st-linkedin">
            <script src="//platform.linkedin.com/in.js" type="text/javascript">
              lang: en_US
            </script>
            <script type="IN/Share" data-counter="right"></script>
          </div>
          
          <div class="st-pinit">
            <script type="text/javascript">
              (function(d){
              var f = d.getElementsByTagName('SCRIPT')[0], p = d.createElement('SCRIPT');
              p.type = 'text/javascript';
              p.async = true;
              p.src = '//assets.pinterest.com/js/pinit.js';
              f.parentNode.insertBefore(p, f);
              }(document));
            </script>
          </div>
        </div>
        <div class="authorArticles">
        </div>
      </div>
      <div class="rssContent">
        <xsl:value-of select="item/content:encoded" disable-output-escaping="yes"/>
      </div>
      <xsl:if test="item/dc:tag">
        <div class="tags" style="display: none;">
          Tags:
          <span>
            <xsl:for-each select="item/dc:tag">
              <xsl:text disable-output-escaping="yes">&lt;a href="</xsl:text><xsl:text disable-output-escaping="yes">/site/ST/blog/tag/</xsl:text>
              <xsl:value-of select="@code"/>
              <xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
              <xsl:value-of select="."/>
              <xsl:text disable-output-escaping="yes">&lt;/a&gt;<xsl:if test="position()!=last()">, </xsl:if></xsl:text>
            </xsl:for-each>
          </span>
        </div>
      </xsl:if>
    </article>
  </xsl:template>
  
  
</xsl:stylesheet>